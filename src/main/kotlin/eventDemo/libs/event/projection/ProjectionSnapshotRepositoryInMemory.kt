package eventDemo.libs.event.projection

import eventDemo.business.event.projection.Projection
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class SnapshotConfig(
  val maxSnapshotCacheSize: Int = 20,
  val maxSnapshotCacheTtl: Duration = 10.minutes,
  /**
   * Only create [snapshots][Projection] every [X][modulo] [events][Event]
   */
  val modulo: Int = 10,
)

class ProjectionSnapshotRepositoryInMemory<E : Event<ID>, P : Projection<ID>, ID : AggregateId>(
  private val eventStore: EventStore<E, ID>,
  private val initialStateBuilder: (aggregateId: ID) -> P,
  private val snapshotCacheConfig: SnapshotConfig = SnapshotConfig(),
  private val applyToProjection: P.(event: E) -> P,
) {
  private val projectionsSnapshot: ConcurrentHashMap<ID, ConcurrentLinkedQueue<Pair<P, Instant>>> = ConcurrentHashMap()

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
  fun applyAndPutToCache(event: E) {
    if ((event.version % snapshotCacheConfig.modulo) == 0) {
      getUntil(event)
        .also {
          save(it)
          removeOldSnapshot(it.aggregateId)
        }
    }
  }

  /**
   * Build the last version of the [Projection] from the cache.
   *
   * 1. get the last snapshot
   * 2. get the missing event to the snapshot
   * 3. apply the missing events to the snapshot
   */
  fun getLast(aggregateId: ID): P {
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
  fun getUntil(event: E): P {
    val lastSnapshot = getLastSnapshotBeforeOrEqualEvent(event)?.first
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

  /**
   * Remove the oldest snapshot.
   *
   * The rules are pass in the controller.
   */
  private fun removeOldSnapshot(aggregateId: ID) {
    projectionsSnapshot[aggregateId]?.let { queue ->
      // never remove the last one
      val theLastOne = getLastSnapshot(aggregateId)
      removeByDate(queue, theLastOne)
      removeBySize(queue, theLastOne)
    }
  }

  private fun removeBySize(
    queue: ConcurrentLinkedQueue<Pair<P, Instant>>,
    theLastOne: Pair<P, Instant>?,
  ) {
    // Remove if size exceeds the limit
    val size = queue.size
    if (size > snapshotCacheConfig.maxSnapshotCacheSize) {
      val numberToRemove = size - snapshotCacheConfig.maxSnapshotCacheSize
      if (numberToRemove > 0) {
        queue
          .sortedBy { it.first.lastEventVersion }
          .take(numberToRemove)
          .let { it - theLastOne }
          .forEach { queue.remove(it) }
      }
    }
  }

  private fun removeByDate(
    queue: ConcurrentLinkedQueue<Pair<P, Instant>>,
    theLastOne: Pair<P, Instant>?,
  ) {
    // remove the oldest by time
    val now = Clock.System.now()
    val deadLine = now - snapshotCacheConfig.maxSnapshotCacheTtl
    val toRemove = queue.filter { deadLine > it.second }
    (toRemove - theLastOne).forEach { queue.remove(it) }
  }

  /**
   * Save the snapshot.
   */
  private fun save(projection: P) {
    projectionsSnapshot
      .computeIfAbsent(projection.aggregateId) { ConcurrentLinkedQueue() }
      .add(Pair(projection, Clock.System.now()))
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
        KotlinLogging.logger { }.warn { "Event is already is the Projection, skip apply." }
        this
      } else {
        error("The version of the event must follow directly after the version of the projection.")
      }
    }
  }
}
