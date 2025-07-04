package eventDemo.business.event.projection.projectionListener

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.PlayerWinEvent
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.GameState
import eventDemo.libs.event.projection.Projection
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import java.util.concurrent.ConcurrentSkipListSet

class ReactionListener(
  private val eventHandler: GameEventHandler,
) {
  companion object Config {
    val registeredListeners = ConcurrentSkipListSet<GameProjectionBus>()
  }

  private val logger = KotlinLogging.logger { }

  fun subscribeToBus(projectionBus: GameProjectionBus) {
    if (registeredListeners.add(projectionBus)) {
      projectionBus.subscribe { projection: Projection<GameId> ->
        if (projection !is GameState) return@subscribe
        withLoggingContext("projection" to projection.toString()) {
          sendStartGameEvent(projection)
          sendWinnerEvent(projection)
        }
      }
    } else {
      "${this::class.simpleName} is already init for this bus".let {
        logger.error { it }
        error(it)
      }
    }
  }

  private suspend fun sendStartGameEvent(state: GameState) {
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
    }
  }

  private suspend fun sendWinnerEvent(state: GameState) {
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
