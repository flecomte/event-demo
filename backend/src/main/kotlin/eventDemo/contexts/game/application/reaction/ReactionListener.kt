package eventDemo.contexts.game.application.reaction

import eventDemo.contexts.game.application.command.handlers.GameEventManager
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.logging.LoggingContextKeys
import eventDemo.contexts.game.application.logging.withLoggingContext
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.game.gameState.Game
import eventDemo.contexts.game.domain.game.gameState.GameCreated
import eventDemo.contexts.game.domain.game.gameState.GameStarted
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentSkipListSet

class ReactionListener(
  gameRepository: GameRepository,
  private val gameEventBus: GameEventBus,
) : GameEventManager(gameRepository, gameEventBus) {
  private companion object Config {
    val registeredListeners = ConcurrentSkipListSet<GameEventBus>()
  }

  private val logger = KotlinLogging.logger { }

  fun subscribeToBus() {
    if (registeredListeners.add(gameEventBus)) {
      gameEventBus.subscribe { event ->
        val game = event.getGame()
        withLoggingContext(LoggingContextKeys.Game to game) {
          sendStartGameEvent(game)
          sendWinnerEvent(game)
        }
      }
    } else {
      "${this::class.simpleName} is already init for this bus".let {
        logger.error { it }
        error(it)
      }
    }
  }

  private fun sendStartGameEvent(game: Game) {
    if (game is GameCreated && game.allPlayerIsReady) {
      game
        .startGame()
        .saveEvents()
        .publishEvents()
    }
  }

  private fun sendWinnerEvent(game: Game) {
    if (game is GameStarted && game.lastPlayerId != null) {
      val lastPlayerWin =
        game
          .players
          .get(game.lastPlayerId)
          .hand.size == 0
      if (lastPlayerWin) {
        game
          .playerWin(game.lastPlayerId)
          .saveEvents()
          .publishEvents()
      }
    }
  }
}
