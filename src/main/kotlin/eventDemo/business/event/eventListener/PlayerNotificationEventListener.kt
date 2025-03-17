package eventDemo.business.event.eventListener

import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.event.CardIsPlayedEvent
import eventDemo.business.event.event.GameEvent
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.event.PlayerChoseColorEvent
import eventDemo.business.event.event.PlayerHavePassEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.event.PlayerWinEvent
import eventDemo.business.event.projection.GameStateRepository
import eventDemo.business.notification.ItsTheTurnOfNotification
import eventDemo.business.notification.Notification
import eventDemo.business.notification.PlayerAsJoinTheGameNotification
import eventDemo.business.notification.PlayerAsPlayACardNotification
import eventDemo.business.notification.PlayerHavePassNotification
import eventDemo.business.notification.PlayerWasChoseTheCardColorNotification
import eventDemo.business.notification.PlayerWasReadyNotification
import eventDemo.business.notification.PlayerWinNotification
import eventDemo.business.notification.TheGameWasStartedNotification
import eventDemo.business.notification.WelcomeToTheGameNotification
import eventDemo.business.notification.YourNewCardNotification
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext

class PlayerNotificationEventListener(
  private val eventBus: GameEventBus,
  private val gameStateRepository: GameStateRepository,
) {
  private val logger = KotlinLogging.logger {}

  fun startListening(
    outgoingNotification: (Notification) -> Unit,
    currentPlayer: Player,
    gameId: GameId,
  ) {
    eventBus.subscribe { event: GameEvent ->
      withLoggingContext("event" to event.toString()) {
        if (event.aggregateId != gameId) {
          return@subscribe
        }

        val currentState = gameStateRepository.getUntil(event)

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
