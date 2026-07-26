package eventDemo.contexts.game.application.notification

import eventDemo.contexts.game.application.notification.models.ItsTheTurnOfNotification
import eventDemo.contexts.game.application.notification.models.Notification
import eventDemo.contexts.game.application.notification.models.PilesShuffledNotification
import eventDemo.contexts.game.application.notification.models.PlayerAsJoinTheGameNotification
import eventDemo.contexts.game.application.notification.models.PlayerAsPlayACardNotification
import eventDemo.contexts.game.application.notification.models.PlayerHavePassNotification
import eventDemo.contexts.game.application.notification.models.PlayerWasReadyNotification
import eventDemo.contexts.game.application.notification.models.PlayerWinNotification
import eventDemo.contexts.game.application.notification.models.TheGameWasStartedNotification
import eventDemo.contexts.game.application.notification.models.WelcomeToTheGameNotification
import eventDemo.contexts.game.application.notification.models.YourNewCardNotification
import eventDemo.contexts.game.domain.events.CardIsPlayedEvent
import eventDemo.contexts.game.domain.events.DrawFilledWithDiscardEvent
import eventDemo.contexts.game.domain.events.GameCreatedEvent
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.events.GameStartedEvent
import eventDemo.contexts.game.domain.events.NewPlayerEvent
import eventDemo.contexts.game.domain.events.PlayerActionEvent
import eventDemo.contexts.game.domain.events.PlayerHaveDrawCardEvent
import eventDemo.contexts.game.domain.events.PlayerReadyEvent
import eventDemo.contexts.game.domain.events.PlayerWinEvent
import eventDemo.contexts.game.domain.game.gameState.Game
import eventDemo.contexts.game.domain.game.gameState.GameStarted
import eventDemo.sharedKernel.UserId
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext

private val logger = KotlinLogging.logger {}

fun GameEvent.toNotification(
  game: Game,
  currentUserId: UserId,
): Iterable<Notification> =
  Iterable {
    iterator {
      context(iterator: SequenceScope<Notification>)
      suspend fun Notification.send() {
        withLoggingContext("notification" to (this).toString()) {
          logger.info { "Notification sent" }
          iterator.yield(this)
        }
      }

      fun PlayerActionEvent.isFromCurrentUser(): Boolean =
        game.players.get(currentUserId).id == playerId

      when (this@toNotification) {
        is GameCreatedEvent -> {
          // Nothing to send
        }

        is DrawFilledWithDiscardEvent -> {
          PilesShuffledNotification().send()
        }

        is NewPlayerEvent -> {
          if (this@toNotification.player.userId != currentUserId) {
            PlayerAsJoinTheGameNotification(
              player = this@toNotification.player,
            ).send()
          } else {
            WelcomeToTheGameNotification(
              players = game.players,
            ).send()
          }
        }

        is CardIsPlayedEvent -> {
          PlayerAsPlayACardNotification(
            playerId = this@toNotification.playerId,
            card = this@toNotification.card,
          ).send()

          if (game is GameStarted) {
            ItsTheTurnOfNotification(
              player = game.nextPlayer,
            ).send()
          }
        }

        is GameStartedEvent -> {
          TheGameWasStartedNotification(
            hand =
              game.players
                .get(currentUserId)
                .hand.cards,
          ).send()

          if (game is GameStarted) {
            ItsTheTurnOfNotification(player = game.nextPlayer)
              .send()
          }
        }

        is PlayerHaveDrawCardEvent -> {
          if (this@toNotification.isFromCurrentUser()) {
            YourNewCardNotification(
              cards = this@toNotification.takenCards,
            ).send()
          } else {
            PlayerHavePassNotification(
              playerId = this@toNotification.playerId,
            ).send()
          }

          if (game is GameStarted) {
            ItsTheTurnOfNotification(player = game.nextPlayer)
              .send()
          }
        }

        is PlayerReadyEvent -> {
          PlayerWasReadyNotification(
            playerId = this@toNotification.playerId,
          ).send()
        }

        is PlayerWinEvent -> {
          PlayerWinNotification(
            playerId = this@toNotification.playerId,
          ).send()
        }
      }
    }
  }
