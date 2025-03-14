package eventDemo.app.event

import eventDemo.app.entity.GameId
import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.VersionBuilder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A stream to publish and read the played card event.
 */
class GameEventHandler(
    private val eventBus: GameEventBus,
    private val eventStore: GameEventStore,
    private val versionBuilder: VersionBuilder,
) : EventHandler<GameEvent, GameId> {
    private val projectionsBuilders: ConcurrentLinkedQueue<(GameEvent) -> Unit> = ConcurrentLinkedQueue()
    private val locks: ConcurrentHashMap<GameId, ReentrantLock> = ConcurrentHashMap()

    override fun registerProjectionBuilder(builder: GameProjectionBuilder) {
        projectionsBuilders.add(builder)
    }

    override fun handle(
        aggregateId: GameId,
        buildEvent: (version: Int) -> GameEvent,
    ): GameEvent =
        locks
            .computeIfAbsent(aggregateId) { ReentrantLock() }
            .withLock {
                buildEvent(versionBuilder.buildNextVersion(aggregateId))
                    .also { eventStore.publish(it) }
            }.also { event ->
                projectionsBuilders.forEach { it(event) }
                eventBus.publish(event)
            }
}

typealias GameProjectionBuilder = (GameEvent) -> Unit
