package eventDemo.app.event

import eventDemo.app.entity.GameId
import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.EventBus
import eventDemo.libs.event.EventStream

/**
 * A stream to publish and read the played card event.
 */
class GameEventStream(
    private val eventBus: EventBus<GameEvent, GameId>,
    private val m: EventStream<GameEvent, GameId>,
) : EventStream<GameEvent, GameId> by m {
    override fun publish(event: GameEvent) {
        m.publish(event)
        eventBus.publish(event)
    }
}
