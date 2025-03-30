package eventDemo.business.event

import eventDemo.business.entity.GameId
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.event.VersionBuilder
import io.github.oshai.kotlinlogging.withLoggingContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Handle the event to dispatch it to store, bus and projections builders
 */
class GameEventHandler(
  private val eventBus: GameEventBus,
  private val eventStore: GameEventStore,
  private val versionBuilder: VersionBuilder,
) : EventHandler<GameEvent, GameId> {
  private val locks: ConcurrentHashMap<GameId, ReentrantLock> = ConcurrentHashMap()

  /**
   * Build Event then send it to the event store and bus.
   */
  override suspend fun handle(
    aggregateId: GameId,
    buildEvent: (version: Int) -> GameEvent,
  ): GameEvent =
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
