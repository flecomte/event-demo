package eventDemo.app.eventListener

import eventDemo.app.entity.GameId
import eventDemo.app.event.buildStateFromEventStream
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.libs.event.EventBus
import eventDemo.libs.event.EventStream

class GameEventReactionListener(
    private val eventBus: EventBus<GameEvent, GameId>,
    private val eventStream: EventStream<GameEvent, GameId>,
) {
    fun init() {
        eventBus.subscribe { event: GameEvent ->
            val state = event.id.buildStateFromEventStream(eventStream)
            if (state.isReady) {
                eventStream.publish(
                    GameStartedEvent.new(
                        state.gameId,
                        state.players,
                    ),
                )
            }
        }
    }
}
