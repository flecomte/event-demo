package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import eventDemo.libs.event.EventStore
import eventDemo.libs.toRanges
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.params.ScanParams
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
        save(it)
        removeOldSnapshot(it.aggregateId, event.version)
      }

  /**
   * Get the list of all [Projections][Projection]
   */
  override fun getList(): List<P> =
    jedis
      .scan(
        "0",
        ScanParams()
          .match(projectionClass.redisKeySearchListLatest)
          .count(100),
      ).result
      .map { jsonToProjection(it) }

  /**
   * Get the last version of the [Projection] from the cache.
   *
   * 1. get the last snapshot
   * 2. get the missing event to the snapshot
   * 3. apply the missing events to the snapshot
   */
  override fun getLast(aggregateId: ID): P =
    jedis
      .get(projectionClass.redisKeyLatest(aggregateId))
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
        .sort(
          projectionClass.redisKey(event.aggregateId),
          SortingParams()
            .desc()
            .by("score")
            .limit(0, 1),
        ).firstOrNull()
        ?.let(jsonToProjection)
    if (lastSnapshot?.lastEventVersion == event.version) {
      return lastSnapshot
    }

    val missingEventOfSnapshot =
      eventStore
        .getStream(event.aggregateId)
        .readVersionBetween((lastSnapshot?.lastEventVersion ?: 1)..event.version)

    return if (lastSnapshot?.lastEventVersion == event.version) {
      lastSnapshot
    } else {
      lastSnapshot.applyEvents(event.aggregateId, missingEventOfSnapshot)
    }
  }

  private fun save(projection: P) {
    jedis.zadd(projection.redisKeyVersion, projection.lastEventVersion.toDouble(), projectionToJson(projection))
    jedis.expire(projection.redisKeyVersion, snapshotCacheConfig.maxSnapshotCacheTtl.inWholeSeconds)
    jedis.set(projection.redisKeyLatest, projectionToJson(projection))
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
        KotlinLogging.logger { }.warn { "Event is already is the Projection, skip apply." }
        this
      } else {
        error("The version of the event must follow directly after the version of the projection.")
      }
    }
  }

  private fun removeOldSnapshot(
    aggregateId: AggregateId,
    lastVersion: Int,
  ) {
    removeByModulo(aggregateId, lastVersion)
    removeTheHeadBySize(aggregateId)
  }

  private fun removeByModulo(
    aggregateId: AggregateId,
    lastVersion: Int,
  ) {
    (lastVersion - snapshotCacheConfig.maxSnapshotCacheSize)
      .let { if (it < 0) 0 else it }
      .let { IntRange(it, lastVersion - 1) }
      .filter { (it % snapshotCacheConfig.modulo) != 1 }
      .toRanges()
      .map {
        jedis.zremrangeByScore(
          projectionClass.redisKey(aggregateId),
          it.min().toDouble(),
          it.max().toDouble(),
        )
      }
  }

  private fun removeTheHeadBySize(aggregateId: AggregateId) {
    val size =
      jedis.zcount(
        projectionClass.redisKey(aggregateId),
        Double.MIN_VALUE,
        Double.MAX_VALUE,
      )

    LongRange((size - snapshotCacheConfig.maxSnapshotCacheSize), size)
      .let {
        jedis.zremrangeByRank(
          projectionClass.redisKey(aggregateId),
          1,
          it.max(),
        )
      }
  }
}

val <P : Projection<*>> KClass<P>.redisKeySearchListLatest: String get() {
  return "projection:$simpleName:*:latest"
}

val <P : Projection<*>> P.redisKeyVersion: String get() {
  return "projection:${this::class.simpleName}:${aggregateId.id}:$lastEventVersion"
}

val <P : Projection<*>> P.redisKeyLatest: String get() {
  return "projection:${this::class.simpleName}:${aggregateId.id}:latest"
}

fun <A : AggregateId, P : Projection<*>> KClass<P>.redisKeyLatest(aggregateId: A): String =
  "projection:$simpleName:${aggregateId.id}:latest"

fun <P : Projection<*>, A : AggregateId> KClass<P>.redisKey(aggregateId: A): String =
  "projection:$simpleName:${aggregateId.id}"
