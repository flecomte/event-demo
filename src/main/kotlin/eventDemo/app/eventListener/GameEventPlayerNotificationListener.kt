package eventDemo.app.eventListener

import eventDemo.app.entity.Card
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventBus
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.event.PlayerChoseColorEvent
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.event.PlayerWinEvent
import eventDemo.app.event.projection.GameStateRepository
import eventDemo.app.notification.ItsTheTurnOfNotification
import eventDemo.app.notification.Notification
import eventDemo.app.notification.PlayerAsJoinTheGameNotification
import eventDemo.app.notification.PlayerAsPlayACardNotification
import eventDemo.app.notification.PlayerHavePassNotification
import eventDemo.app.notification.PlayerWasChoseTheCardColorNotification
import eventDemo.app.notification.PlayerWasReadyNotification
import eventDemo.app.notification.PlayerWinNotification
import eventDemo.app.notification.TheGameWasStartedNotification
import eventDemo.app.notification.WelcomeToTheGameNotification
import eventDemo.app.notification.YourNewCardNotification
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

class GameEventPlayerNotificationListener(
    private val eventBus: GameEventBus,
    private val gameStateRepository: GameStateRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun startListening(
        outgoingNotificationChannel: SendChannel<Notification>,
        currentPlayer: Player,
    ) {
        eventBus.subscribe { event: GameEvent ->
            val currentState = gameStateRepository.getUntil(event)

            fun Notification.send() {
                if (currentState.players.contains(currentPlayer)) {
                    // Only notify players who have already joined the game.
                    outgoingNotificationChannel.trySendBlocking(this)
                    logger.atInfo {
                        message = "Notification for player ${currentPlayer.name} was SEND: ${this@send}"
                        payload = mapOf("notification" to this@send, "event" to event)
                    }
                } else {
                    // Rare use case, when a connexion is created with the channel,
                    // but the player was not already join in the game
                    logger.atWarn {
                        message = "Notification for player ${currentPlayer.name} was SKIP, No player on the game: ${this@send}"
                        payload = mapOf("notification" to this@send, "event" to event)
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
