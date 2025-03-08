package eventDemo.app.eventListener

import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.buildStateFromEventStreamTo
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import io.github.oshai.kotlinlogging.KotlinLogging

class GameEventReactionListener(
    private val eventBus: GameEventBus,
    private val eventStream: GameEventStream,
    private val priority: Int = DEFAULT_PRIORITY,
) {
    companion object Config {
        const val DEFAULT_PRIORITY = -1000
    }

    private val logger = KotlinLogging.logger { }

    fun init() {
        eventBus.subscribe(priority) { event: GameEvent ->
            val state = event.buildStateFromEventStreamTo(eventStream)
            if (state.isReady && !state.isStarted) {
                val reactionEvent =
                    GameStartedEvent.new(
                        state.gameId,
                        state.players,
                    )
                logger.atInfo {
                    message = "Event Send on reaction of: $event"
                    payload =
                        mapOf(
                            "event" to event,
                            "reactionEvent" to reactionEvent,
                        )
                }
                eventStream.publish(reactionEvent)
            }
        }
    }
}
