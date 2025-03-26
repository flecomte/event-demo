package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import eventDemo.libs.event.EventStore
import eventDemo.libs.toRanges
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.params.SortingParams
import kotlin.reflect.KClass

class ProjectionSnapshotRepositoryInRedis<E : Event<ID>, P : Projection<ID>, ID : AggregateId>(
  private val eventStore: EventStore<E, ID>,
  private val jedis: UnifiedJedis,
  private val initialStateBuilder: (aggregateId: ID) -> P,
  private val snapshotCacheConfig: SnapshotConfig = SnapshotConfig(),
  private val projectionClass: KClass<P>,
  private val projectionToJson: (P) -> String,
  private val jsonToProjection: (String) -> P,
  private val applyToProjection: P.(event: E) -> P,
) : ProjectionSnapshotRepository<E, P, ID> {
  val logger = KotlinLogging.logger { }

  /**
   * Create a snapshot for the event
   *
   * 1. get the last snapshot with a version lower than that of the event
   * 2. get the events with a greater version of the snapshot
   * 3. apply the event to the snapshot
   * 4. apply the new event to the projection
   * 5. save it
   * 6. remove old one
   */
  override fun applyAndPutToCache(event: E): P =
    getUntil(event)
      .also {
        withLoggingContext(mapOf("projection" to it.toString(), "event" to event.toString())) {
          save(it)
          removeOldSnapshot(it.aggregateId, event.version)
        }
      }

  override fun count(aggregateId: ID): Int =
    jedis.zcount(projectionClass.redisKey(aggregateId), Double.MIN_VALUE, Double.MAX_VALUE).toInt()

  override fun countAll(): Int =
    jedis.zcount(projectionClass.redisKey, Double.MIN_VALUE, Double.MAX_VALUE).toInt()

  /**
   * Get the list of all [Projections][Projection]
   */
  override fun getList(
    limit: Int,
    offset: Int,
  ): List<P> =
    jedis
      .sort(
        projectionClass.redisKeySearchList,
        SortingParams()
          .desc()
          .by("score")
          .limit(limit, offset),
      ).map { jsonToProjection(it) }

  /**
   * Get the last version of the [Projection] from the cache.
   *
   * 1. get the last snapshot
   * 2. get the missing event to the snapshot
   * 3. apply the missing events to the snapshot
   */
  override fun getLast(aggregateId: ID): P =
    jedis
      .sort(
        projectionClass.redisKey(aggregateId),
        SortingParams()
          .desc()
          .by("score")
          .limit(0, 1),
      ).firstOrNull()
      ?.let(jsonToProjection)
      ?: initialStateBuilder(aggregateId)

  /**
   * Build the [Projection] to the specific [event][Event].
   *
   * It does not contain the [events][Event] it after this one.
   *
   * 1. get the last snapshot before the event
   * 2. get the events with a greater version of the snapshot but lower of passed event
   * 3. apply the events to the snapshot
   */
  override fun getUntil(event: E): P {
    val lastSnapshot =
      jedis
        .zrangeByScore(
          projectionClass.redisKey(event.aggregateId),
          1.0,
          event.version.toDouble(),
          0,
          1,
        ).firstOrNull()
        ?.let(jsonToProjection)
    if (lastSnapshot?.lastEventVersion == event.version) {
      return lastSnapshot
    }
    if (lastSnapshot != null && lastSnapshot.lastEventVersion > event.version) {
      logger.error { "Cannot be apply event on more recent snapshot" }
      error("Cannot be apply event on more recent snapshot")
    }

    val missingEventOfSnapshot =
      eventStore
        .getStream(event.aggregateId)
        // take the last snapshot version +1 to event version
        .readVersionBetween(lastSnapshot, event)

    return if (lastSnapshot?.lastEventVersion == event.version) {
      lastSnapshot
    } else {
      lastSnapshot.applyEvents(event.aggregateId, missingEventOfSnapshot)
    }
  }

  private fun save(projection: P) {
    repeat(5) {
      val added = jedis.zadd(projection.redisKey, projection.lastEventVersion.toDouble(), projectionToJson(projection))
      if (added < 1) {
        logger.error { "Projection NOT saved" }
      } else {
        logger.info { "Projection saved" }
        return
      }
    }
    jedis.expire(projection.redisKey, snapshotCacheConfig.maxSnapshotCacheTtl.inWholeSeconds)
  }

  /**
   * Apply events to the projection.
   */
  private fun P?.applyEvents(
    aggregateId: ID,
    eventsToApply: Set<E>,
  ): P =
    eventsToApply.fold(this ?: initialStateBuilder(aggregateId), applyToProjectionSecure)

  /**
   * Wrap the [applyToProjection] lambda to avoid duplicate apply of the same event.
   */
  private val applyToProjectionSecure: P.(event: E) -> P = { event ->
    withLoggingContext("event" to event.toString(), "projection" to this.toString()) {
      if (event.version == lastEventVersion + 1) {
        applyToProjection(event)
      } else if (event.version <= lastEventVersion) {
        KotlinLogging.logger { }.warn { "Event is already in the Projection, skip apply." }
        this
      } else {
        error("The version of the event must follow directly after the version of the projection.")
      }
    }
  }

  fun removeOldSnapshot(
    aggregateId: AggregateId,
    lastVersion: Int,
  ) {
    if (snapshotCacheConfig.enabled) {
      removeByModulo(aggregateId, lastVersion)
      removeTheHeadBySize(aggregateId, lastVersion)
    }
  }

  private fun removeByModulo(
    aggregateId: AggregateId,
    lastVersion: Int,
  ) {
    (lastVersion - (snapshotCacheConfig.maxSnapshotCacheSize * snapshotCacheConfig.modulo))
      .let { if (it < 2) 2 else it }
      .let { IntRange(it, lastVersion - 1) }
      .filter { (it % snapshotCacheConfig.modulo) != 1 }
      .toRanges()
      .map {
        jedis
          .zremrangeByScore(
            projectionClass.redisKey(aggregateId),
            it.first.toDouble(),
            it.last.toDouble(),
          ).also { removedCount ->
            if (removedCount > 0) {
              logger.info {
                "$removedCount snapshot removed Modulo(${snapshotCacheConfig.modulo}) (${it.first} to ${it.last}) [lastVersion=$lastVersion]"
              }
            }
          }
      }
  }

  private fun removeTheHeadBySize(
    aggregateId: AggregateId,
    lastVersion: Int,
  ) {
    (lastVersion - (snapshotCacheConfig.maxSnapshotCacheSize * snapshotCacheConfig.modulo))
      .toDouble()
      .let {
        jedis
          .zremrangeByScore(
            projectionClass.redisKey(aggregateId),
            2.0,
            it,
          ).also { removedCount ->
            if (removedCount > 0) {
              logger.info {
                "$removedCount snapshot removed Size(${snapshotCacheConfig.maxSnapshotCacheSize}) (1.0 to $it) [lastVersion=$lastVersion]"
              }
            }
          }
      }
  }
}

val <P : Projection<*>> KClass<P>.redisKeySearchList: String get() {
  return "projection:$simpleName:*"
}

val <P : Projection<*>> P.redisKey: String get() {
  return "projection:${this::class.simpleName}:${aggregateId.id}"
}

fun <P : Projection<*>, A : AggregateId> KClass<P>.redisKey(aggregateId: A): String =
  "projection:$simpleName:${aggregateId.id}"

val <P : Projection<*>> KClass<P>.redisKey: String get() =
  "projection:$simpleName"
