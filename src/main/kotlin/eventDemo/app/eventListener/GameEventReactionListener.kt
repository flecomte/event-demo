package eventDemo.app.eventListener

import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.buildStateFromEventStream
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent

class GameEventReactionListener(
    private val eventBus: GameEventBus,
    private val eventStream: GameEventStream,
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
