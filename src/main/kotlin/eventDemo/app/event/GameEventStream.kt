package eventDemo.app.event

import eventDemo.app.entity.GameId
import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.EventStream

/**
 * A stream to publish and read the played card event.
 */
class GameEventStream(
    private val eventBus: GameEventBus,
    private val eventStream: EventStream<GameEvent, GameId>,
) : EventStream<GameEvent, GameId> by eventStream {
    override fun publish(event: GameEvent) {
        eventStream.publish(event)
        eventBus.publish(event)
    }
}
