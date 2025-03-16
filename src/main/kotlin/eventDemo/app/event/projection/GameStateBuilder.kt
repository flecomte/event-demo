package eventDemo.app.event.projection

import eventDemo.app.entity.Card
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.event.PlayerActionEvent
import eventDemo.app.event.event.PlayerChoseColorEvent
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.event.PlayerWinEvent
import io.github.oshai.kotlinlogging.KotlinLogging

fun GameState.apply(event: GameEvent): GameState =
  this.let { state ->
    val logger = KotlinLogging.logger { }
    if (event is PlayerActionEvent) {
      if (state.currentPlayerTurn != event.player) {
        logger.atError {
          message = "Inconsistent player turn"
          payload =
            mapOf(
              "CurrentPlayerTurn" to (state.currentPlayerTurn ?: "No currentPlayerTurn"),
              "Player" to event.player,
            )
        }
      }
    }

    when (event) {
      is CardIsPlayedEvent -> {
        val nextDirectionAfterPlay =
          when (event.card) {
            is Card.ReverseCard -> state.direction.revert()
            else -> state.direction
          }

        val color =
          when (event.card) {
            is Card.ColorCard -> event.card.color
            is Card.AllColorCard -> null
          }

        val currentPlayerAfterThePlay =
          if (event.card is Card.AllColorCard) {
            state.currentPlayerTurn
          } else {
            state.nextPlayer(nextDirectionAfterPlay)
          }

        state.copy(
          currentPlayerTurn = currentPlayerAfterThePlay,
          direction = nextDirectionAfterPlay,
          colorOnCurrentStack = color,
          cardOnCurrentStack = GameState.LastCard(event.card, event.player),
          deck = state.deck.putOneCardFromHand(event.player, event.card),
        )
      }

      is NewPlayerEvent -> {
        if (state.isStarted) {
          logger.error { "The game is already started" }
        }

        state.copy(
          players = state.players + event.player,
        )
      }

      is PlayerReadyEvent -> {
        if (state.isStarted) {
          logger.error { "The game is already started" }
        }
        state.copy(
          readyPlayers = state.readyPlayers + event.player,
        )
      }

      is PlayerHavePassEvent -> {
        if (event.takenCard != state.deck.stack.first()) {
          logger.error { "taken card is not ot top of the stack: ${event.takenCard}" }
        }
        state.copy(
          currentPlayerTurn = state.nextPlayerTurn,
          deck = state.deck.takeOneCardFromStackTo(event.player),
        )
      }

      is PlayerChoseColorEvent -> {
        state.copy(
          currentPlayerTurn = state.nextPlayerTurn,
          colorOnCurrentStack = event.color,
        )
      }

      is GameStartedEvent -> {
        state.copy(
          colorOnCurrentStack = (event.deck.discard.first() as? Card.ColorCard)?.color ?: state.colorOnCurrentStack,
          cardOnCurrentStack = GameState.LastCard(event.deck.discard.first(), event.firstPlayer),
          currentPlayerTurn = event.firstPlayer,
          deck = event.deck,
          isStarted = true,
        )
      }

      is PlayerWinEvent -> {
        state.copy(
          playerWins = state.playerWins + event.player,
        )
      }
    }.copy(
      lastEventVersion = event.version,
    )
  }
