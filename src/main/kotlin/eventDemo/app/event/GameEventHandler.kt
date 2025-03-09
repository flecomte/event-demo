package eventDemo.app.event

import eventDemo.app.event.event.GameEvent

/**
 * A stream to publish and read the played card event.
 */
class GameEventHandler(
    private val eventBus: GameEventBus,
    private val eventStream: GameEventStream,
) {
    private val projectionsBuilders: MutableList<(GameEvent) -> Unit> = mutableListOf()

    fun registerProjectionBuilder(builder: GameProjectionBuilder) {
        projectionsBuilders.add(builder)
    }

    fun handle(vararg events: GameEvent) {
        events.forEach { event ->
            eventStream.publish(event)
            projectionsBuilders.forEach { it(event) }
            eventBus.publish(event)
        }
    }
}

typealias GameProjectionBuilder = (GameEvent) -> Unit
