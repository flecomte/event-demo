package eventDemo.libs.event

import eventDemo.libs.bus.Bus
import io.github.oshai.kotlinlogging.withLoggingContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Handle the event to dispatch it to store, bus and projections builders
 */
class EventHandlerImpl<E : Event<ID>, ID : AggregateId>(
  private val eventBus: Bus<E>,
  private val eventStore: EventStore<E, ID>,
  private val versionBuilder: VersionBuilder,
) : EventHandler<E, ID> {
  private val locks: ConcurrentHashMap<ID, ReentrantLock> = ConcurrentHashMap()

  /**
   * Build Event then send it to the event store and bus.
   */
  override suspend fun handle(
    aggregateId: ID,
    buildEvent: (version: Int) -> E,
  ): E =
    withLoggingContext("aggregateId" to aggregateId.toString()) {
      locks
        // Get lock for the aggregate
        .computeIfAbsent(aggregateId) { ReentrantLock() }
        .withLock {
          // Build event with the version
          buildEvent(versionBuilder.buildNextVersion(aggregateId))
            // then publish it to the event store
            .also {
              withLoggingContext("event" to it.toString()) {
                eventStore.publish(it)
              }
            }
        }.also { event ->
          withLoggingContext("event" to event.toString()) {
            // Publish to the bus
            eventBus.publish(event)
          }
        }
    }
}
