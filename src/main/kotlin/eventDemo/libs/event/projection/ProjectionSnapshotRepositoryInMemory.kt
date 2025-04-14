package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import eventDemo.libs.event.EventStore
import eventDemo.libs.event.EventStream
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.reflect.KClass

class ProjectionSnapshotRepositoryInMemory<E : Event<ID>, P : Projection<ID>, ID : AggregateId>(
  val name: KClass<*> = ProjectionSnapshotRepositoryInMemory::class,
  private val eventStore: EventStore<E, ID>,
  private val initialStateBuilder: (aggregateId: ID) -> P,
  private val snapshotCacheConfig: SnapshotConfig = SnapshotConfig(),
  private val applyToProjection: P.(event: E) -> P,
) : ProjectionSnapshotRepository<E, P, ID> {
  private val projectionsSnapshot: ConcurrentHashMap<ID, ConcurrentLinkedQueue<Pair<P, Instant>>> = ConcurrentHashMap()
  private val logger = KotlinLogging.logger(name.qualifiedName.toString())

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
  override suspend fun applyAndPutToCache(event: E): P =
    getUntil(event)
      .also {
        withLoggingContext("projection" to it.toString()) {
          save(it)
          removeOldSnapshot(it.aggregateId)
        }
      }

  override fun count(aggregateId: ID): Int =
    projectionsSnapshot[aggregateId]?.count() ?: 0

  override fun countAll(): Int =
    projectionsSnapshot.mappingCount().toInt()

  /**
   * Build the list of all [Projections][Projection]
   */
  override fun getList(
    limit: Int,
    offset: Int,
  ): List<P> =
    projectionsSnapshot
      .map { (id, b) ->
        getLast(id)
      }.drop(offset)
      .take(limit)

  /**
   * Build the last version of the [Projection] from the cache.
   *
   * 1. get the last snapshot
   * 2. get the missing event to the snapshot
   * 3. apply the missing events to the snapshot
   */
  override fun getLast(aggregateId: ID): P {
    val lastSnapshot = getLastSnapshot(aggregateId)?.first
    val missingEventOfSnapshot = getEventAfterTheSnapshot(aggregateId, lastSnapshot)
    return lastSnapshot.applyEvents(aggregateId, missingEventOfSnapshot)
  }

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
    val lastSnapshot = getLastSnapshotBeforeOrEqualEvent(event)?.first
    if (lastSnapshot?.lastEventVersion == event.version) {
      return lastSnapshot
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

  /**
   * Remove the oldest [snapshot][P] of the [queue][projectionsSnapshot].
   *
   * The rules are pass in the controller.
   */
  private fun removeOldSnapshot(aggregateId: ID) {
    projectionsSnapshot[aggregateId]?.let { queue ->
      if (snapshotCacheConfig.enabled) {
        queue
          .excludeFirstAndLast()
          .excludeTheHeadBySize()
          .excludeNewerByDate()
          .excludeByModulo()
          .forEach { queue.remove(it) }
      }
    }
  }

  /**
   * Return a new list without the first and last snapshot.
   *
   * Exclude from deletion the first and the last.
   */
  private fun FilteredList<P>.excludeFirstAndLast(): FilteredList<P> =
    sortedBy { it.first.lastEventVersion }
      .drop(1)
      .dropLast(1)

  /**
   * Return a new list of event filtered by the version modulo.
   *
   * Exclude from deletion 1 element out of 10 (if modulo 10 in [config][snapshotCacheConfig]).
   */
  private fun FilteredList<P>.excludeByModulo(): FilteredList<P> =
    filter { (it.first.lastEventVersion % snapshotCacheConfig.modulo) != 1 }

  /**
   * Return a new list of event filtered by the maximum size.
   *
   * Exclude from removal all [snapshot][projectionsSnapshot] that in the head of the queue.
   */
  private fun FilteredList<P>.excludeTheHeadBySize(): FilteredList<P> {
    // filter if size exceeds the limit
    return sortedBy { it.first.lastEventVersion }
      .dropLast(snapshotCacheConfig.maxSnapshotCacheSize)
  }

  /**
   * Return a new list of event filtered by the maximum date.
   *
   * Exclude from removal all [snapshot][projectionsSnapshot] that newer of the date (in [config][SnapshotConfig]).
   */
  private fun FilteredList<P>.excludeNewerByDate(): FilteredList<P> {
    val now = Clock.System.now()
    val deadLine = now - snapshotCacheConfig.maxSnapshotCacheTtl
    return filter { deadLine < it.second }
  }

  /**
   * Save the snapshot.
   */
  private fun save(projection: P) {
    projectionsSnapshot
      .computeIfAbsent(projection.aggregateId) { ConcurrentLinkedQueue() }
      .add(Pair(projection, Clock.System.now()))
      .also { logger.info { "Projection saved" } }
  }

  /**
   * Get the last snapshot when the version is lower of then event version
   */
  private fun getLastSnapshotBeforeOrEqualEvent(event: E) =
    projectionsSnapshot[event.aggregateId]
      ?.sortedByDescending { it.first.lastEventVersion }
      ?.find { it.first.lastEventVersion <= event.version }

  /**
   * Get the last snapshot (with the higher version).
   */
  private fun getLastSnapshot(aggregateId: ID) =
    projectionsSnapshot[aggregateId]
      ?.maxByOrNull { it.first.lastEventVersion }

  /**
   * Get the events from the [event stream][EventStream] when the version is higher of the snapshot.
   *
   * If the snapshot is null, it takes all events from the event [event stream][EventStream]
   */
  private fun getEventAfterTheSnapshot(
    aggregateId: ID,
    snapshot: P?,
  ) =
    eventStore
      .getStream(aggregateId)
      .readGreaterOfVersion(snapshot?.lastEventVersion ?: 0)

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
}

private typealias FilteredList<P> = Collection<Pair<P, Instant>>
