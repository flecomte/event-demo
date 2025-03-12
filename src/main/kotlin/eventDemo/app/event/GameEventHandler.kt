package eventDemo.app.event

import eventDemo.app.entity.GameId
import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.VersionBuilder

/**
 * A stream to publish and read the played card event.
 */
class GameEventHandler(
    private val eventBus: GameEventBus,
    private val eventStream: GameEventStream,
    private val versionBuilder: VersionBuilder,
) : EventHandler<GameEvent, GameId> {
    private val projectionsBuilders: MutableList<(GameEvent) -> Unit> = mutableListOf()

    override fun registerProjectionBuilder(builder: GameProjectionBuilder) {
        projectionsBuilders.add(builder)
    }

    override fun handle(buildEvent: (version: Int) -> GameEvent): GameEvent =
        buildEvent(versionBuilder.buildNextVersion()).also { event ->
            eventStream.publish(event)
            projectionsBuilders.forEach { it(event) }
            eventBus.publish(event)
        }
}

typealias GameProjectionBuilder = (GameEvent) -> Unit
