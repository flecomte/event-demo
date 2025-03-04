package eventDemo.shared.event

import eventDemo.libs.event.EventBus
import eventDemo.libs.event.EventStream
import eventDemo.shared.GameId

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
