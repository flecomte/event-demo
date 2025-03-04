package eventDemo.app.actions

import eventDemo.libs.event.EventBus
import eventDemo.libs.event.EventStream
import eventDemo.shared.GameId
import eventDemo.shared.event.GameEvent
import eventDemo.shared.event.GameStartedEvent
import eventDemo.shared.event.buildStateFromEventStream

class GameEventReactionSubscriber(
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
