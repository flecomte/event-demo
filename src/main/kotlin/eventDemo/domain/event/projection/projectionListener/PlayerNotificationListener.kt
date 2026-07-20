package eventDemo.domain.event.projection.projectionListener

import eventDemo.domain.entity.Card
import eventDemo.domain.entity.GameId
import eventDemo.domain.entity.Player
import eventDemo.domain.event.event.CardIsPlayedEvent
import eventDemo.domain.event.event.GameStartedEvent
import eventDemo.domain.event.event.NewPlayerEvent
import eventDemo.domain.event.event.PlayerChoseColorEvent
import eventDemo.domain.event.event.PlayerHavePassEvent
import eventDemo.domain.event.event.PlayerReadyEvent
import eventDemo.domain.event.event.PlayerWinEvent
import eventDemo.domain.event.projection.GameProjectionBus
import eventDemo.domain.event.projection.GameState
import eventDemo.domain.notification.ItsTheTurnOfNotification
import eventDemo.domain.notification.Notification
import eventDemo.domain.notification.PlayerAsJoinTheGameNotification
import eventDemo.domain.notification.PlayerAsPlayACardNotification
import eventDemo.domain.notification.PlayerHavePassNotification
import eventDemo.domain.notification.PlayerWasChoseTheCardColorNotification
import eventDemo.domain.notification.PlayerWasReadyNotification
import eventDemo.domain.notification.PlayerWinNotification
import eventDemo.domain.notification.TheGameWasStartedNotification
import eventDemo.domain.notification.WelcomeToTheGameNotification
import eventDemo.domain.notification.YourNewCardNotification
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext

class PlayerNotificationListener(
  private val projectionBus: GameProjectionBus,
) {
  private val logger = KotlinLogging.logger {}

  /**
   * Forward projection from [bus][GameProjectionBus] to the player [notification][outgoingNotification]
   */
  fun startListening(
    currentPlayer: Player,
    gameId: GameId,
    outgoingNotification: (Notification) -> Unit,
  ): AutoCloseable {
    return projectionBus.subscribe { currentState ->
      if (currentState !is GameState) return@subscribe
      if (currentState.aggregateId != gameId) return@subscribe
      withLoggingContext("currentPlayer" to currentPlayer.toString(), "projection" to currentState.toString()) {
        fun Notification.send() {
          withLoggingContext("notification" to this.toString()) {
            if (currentState.players.contains(currentPlayer)) {
              // Only notify players who have already joined the game.
              outgoingNotification(this)
              logger.info { "Notification was SEND" }
            } else {
              // Rare use case, when a connexion is created with the channel,
              // but the player was not already join in the game
              logger.warn { "Notification was SKIP, no player on the game" }
            }
          }
        }

        fun sendNextTurnNotif() =
          ItsTheTurnOfNotification(
            player = currentState.currentPlayerTurn ?: error("No player turn defined"),
          ).send()

        val event =
          currentState.lastEvent
            ?: error("No last event in the GameState projection")

        when (event) {
          is NewPlayerEvent -> {
            if (currentPlayer != event.player) {
              PlayerAsJoinTheGameNotification(
                player = event.player,
              ).send()
            } else {
              WelcomeToTheGameNotification(
                players = currentState.players,
              ).send()
            }
          }

          is CardIsPlayedEvent -> {
            if (currentPlayer != event.player) {
              PlayerAsPlayACardNotification(
                player = event.player,
                card = event.card,
              ).send()
            }

            if (event.card !is Card.AllColorCard) {
              ItsTheTurnOfNotification(
                player = currentState.currentPlayerTurn ?: error("No player turn defined"),
              ).send()
            }
          }

          is GameStartedEvent -> {
            TheGameWasStartedNotification(
              hand =
                event.deck.playersHands.getHand(currentPlayer)
                  ?: error("You are not in the game"),
            ).send()

            sendNextTurnNotif()
          }

          is PlayerChoseColorEvent -> {
            if (currentPlayer != event.player) {
              PlayerWasChoseTheCardColorNotification(
                player = event.player,
                color = event.color,
              ).send()
            }

            sendNextTurnNotif()
          }

          is PlayerHavePassEvent -> {
            if (currentPlayer == event.player) {
              YourNewCardNotification(
                card = event.takenCard,
              ).send()
            } else {
              PlayerHavePassNotification(
                player = event.player,
              ).send()
            }

            sendNextTurnNotif()
          }

          is PlayerReadyEvent -> {
            if (currentPlayer != event.player) {
              PlayerWasReadyNotification(
                player = event.player,
              ).send()
            }
          }

          is PlayerWinEvent -> {
            PlayerWinNotification(
              player = event.player,
            ).send()
          }
        }
      }
    }
  }
}
