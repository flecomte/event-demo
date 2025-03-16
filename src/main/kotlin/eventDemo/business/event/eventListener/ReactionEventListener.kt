package eventDemo.business.event.eventListener

import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.GameEvent
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.event.PlayerWinEvent
import eventDemo.business.event.projection.GameState
import eventDemo.business.event.projection.GameStateRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext

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
      withLoggingContext("event" to event.toString()) {
        val state = gameStateRepository.getUntil(event)
        sendStartGameEvent(state, event)
        sendWinnerEvent(state)
      }
    }
  }

  private fun sendStartGameEvent(
    state: GameState,
    event: GameEvent,
  ) {
    if (state.isReady && !state.isStarted) {
      val reactionEvent =
        eventHandler.handle(state.aggregateId) {
          GameStartedEvent.new(
            id = state.aggregateId,
            players = state.players,
            version = it,
          )
        }
      logger.atInfo {
        message = "Reaction event was Send"
        payload = mapOf("reactionEvent" to reactionEvent)
      }
    } else {
      if (event is PlayerReadyEvent) {
        logger.info { "All players was not ready ${state.readyPlayers}" }
      }
    }
  }

  private fun sendWinnerEvent(state: GameState) {
    val winner = state.playerHasNoCardLeft().firstOrNull()
    if (winner != null) {
      val reactionEvent =
        eventHandler.handle(state.aggregateId) {
          PlayerWinEvent(
            aggregateId = state.aggregateId,
            player = winner,
            version = it,
          )
        }

      logger.atInfo {
        message = "Reaction event was Send"
        payload = mapOf("reactionEvent" to reactionEvent)
      }
    }
  }
}
