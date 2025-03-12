package eventDemo.app.eventListener

import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.event.PlayerWinEvent
import eventDemo.app.event.projection.GameState
import eventDemo.app.event.projection.GameStateRepository
import io.github.oshai.kotlinlogging.KotlinLogging

class ReactionEventListener(
    private val eventBus: GameEventBus,
    private val eventHandler: GameEventHandler,
    private val gameStateRepository: GameStateRepository,
    private val priority: Int = DEFAULT_PRIORITY,
) {
    companion object Config {
        const val DEFAULT_PRIORITY = -1000
    }

    private val logger = KotlinLogging.logger { }

    fun init() {
        eventBus.subscribe(priority) { event: GameEvent ->
            val state = gameStateRepository.getUntil(event)
            sendStartGameEvent(state, event)
            sendWinnerEvent(state, event)
        }
    }

    private suspend fun sendStartGameEvent(
        state: GameState,
        event: GameEvent,
    ) {
        if (state.isReady && !state.isStarted) {
            val reactionEvent =
                eventHandler.handle {
                    GameStartedEvent.new(
                        id = state.aggregateId,
                        players = state.players,
                        version = it,
                    )
                }
            logger.atInfo {
                message = "Reaction event was Send $reactionEvent on reaction of: $event"
                payload =
                    mapOf(
                        "event" to event,
                        "reactionEvent" to reactionEvent,
                    )
            }
        } else {
            if (event is PlayerReadyEvent) {
                logger.info { "All players was not ready ${state.readyPlayers}" }
            }
        }
    }

    private fun sendWinnerEvent(
        state: GameState,
        event: GameEvent,
    ) {
        val winner = state.playerHasNoCardLeft().firstOrNull()
        if (winner != null) {
            val reactionEvent =
                eventHandler.handle {
                    PlayerWinEvent(
                        aggregateId = state.aggregateId,
                        player = winner,
                        version = it,
                    )
                }

            logger.atInfo {
                message = "Reaction event was Send $reactionEvent on reaction of: $event"
                payload =
                    mapOf(
                        "event" to event,
                        "reactionEvent" to reactionEvent,
                    )
            }
        }
    }
}
