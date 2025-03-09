package eventDemo.app.eventListener

import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.event.PlayerChoseColorEvent
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.event.PlayerWinEvent
import eventDemo.app.event.projection.buildStateFromEventStreamTo
import eventDemo.app.notification.PlayerAsJoinTheGameNotification
import eventDemo.app.notification.PlayerAsPlayACardNotification
import eventDemo.app.notification.PlayerHavePassNotification
import eventDemo.app.notification.PlayerWasChoseTheCardColorNotification
import eventDemo.app.notification.PlayerWasReadyNotification
import eventDemo.app.notification.PlayerWinNotification
import eventDemo.app.notification.TheGameWasStartedNotification
import eventDemo.app.notification.WelcomeToTheGameNotification
import eventDemo.app.notification.YourNewCardNotification
import eventDemo.shared.toFrame
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

class GameEventPlayerNotificationListener(
    private val eventBus: GameEventBus,
    private val eventStream: GameEventStream,
) {
    private val logger = KotlinLogging.logger {}

    fun startListening(
        outgoing: SendChannel<Frame>,
        currentPlayer: Player,
    ) {
        eventBus.subscribe { event: GameEvent ->
            val currentState = event.buildStateFromEventStreamTo(eventStream)
            val notification =
                when (event) {
                    is NewPlayerEvent -> {
                        if (currentPlayer != event.player) {
                            PlayerAsJoinTheGameNotification(
                                player = event.player,
                            )
                        } else {
                            WelcomeToTheGameNotification(
                                players = currentState.players,
                            )
                        }
                    }

                    is CardIsPlayedEvent -> {
                        if (currentPlayer != event.player) {
                            PlayerAsPlayACardNotification(
                                player = event.player,
                                card = event.card,
                            )
                        } else {
                            null
                        }
                    }

                    is GameStartedEvent -> {
                        TheGameWasStartedNotification(
                            hand =
                                event.deck.playersHands.getHand(currentPlayer)
                                    ?: error("You are not in the game"),
                        )
                    }

                    is PlayerChoseColorEvent -> {
                        if (currentPlayer != event.player) {
                            PlayerWasChoseTheCardColorNotification(
                                player = event.player,
                                color = event.color,
                            )
                        } else {
                            null
                        }
                    }

                    is PlayerHavePassEvent -> {
                        if (currentPlayer == event.player) {
                            YourNewCardNotification(
                                card = event.takenCard,
                            )
                        } else {
                            PlayerHavePassNotification(
                                player = event.player,
                            )
                        }
                    }

                    is PlayerReadyEvent -> {
                        if (currentPlayer != event.player) {
                            PlayerWasReadyNotification(
                                player = event.player,
                            )
                        } else {
                            null
                        }
                    }

                    is PlayerWinEvent -> {
                        PlayerWinNotification(
                            player = event.player,
                        )
                    }
                }

            if (notification == null) {
                logger.atInfo {
                    message = "Notification Ignore: $event"
                    payload = mapOf("event" to event)
                }
            } else if (currentState.players.contains(currentPlayer)) {
                // Only notify players who have already joined the game.
                outgoing.trySendBlocking(notification.toFrame())
                logger.atInfo {
                    message = "Notification SEND: $notification"
                    payload = mapOf("notification" to notification, "event" to event)
                }
            } else {
                logger.atInfo {
                    message = "Notification SKIP: $notification"
                    payload = mapOf("notification" to notification, "event" to event)
                }
            }
        }
    }
}
