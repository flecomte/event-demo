package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.app.command.command.ICantPlayCommand
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.command.command.IWantToPlayCardCommand
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.projection.GameStateRepository
import eventDemo.app.notification.ErrorNotification
import eventDemo.app.notification.Notification
import eventDemo.libs.command.CommandStreamChannelBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

/**
 * Listen [GameCommand] on [GameCommandStream], check the validity and execute an action.
 *
 * This action can be executing an action and produce a new [GameEvent] after verification.
 */
class GameCommandHandler(
    private val eventHandler: GameEventHandler,
    private val gameStateRepository: GameStateRepository,
    private val commandStreamChannel: CommandStreamChannelBuilder<GameCommand>,
) {
    private val logger = KotlinLogging.logger { }

    /**
     * Init the handler
     */
    suspend fun handle(
        player: Player,
        incomingCommandChannel: ReceiveChannel<GameCommand>,
        outgoingErrorChannelNotification: SendChannel<Notification>,
    ) = commandStreamChannel(incomingCommandChannel)
        .process { command ->
            if (command.payload.player.id != player.id) {
                logger.atWarn {
                    message = "Handle command Refuse, the player of the command is not the same: $command"
                    payload = mapOf("command" to command)
                }
                nack()
            } else {
                logger.atInfo {
                    message = "Handle command: $command"
                    payload = mapOf("command" to command)
                }
                command.run(outgoingErrorChannelNotification)
            }
        }

    private suspend fun GameCommand.run(outgoingErrorChannelNotification: SendChannel<Notification>) {
        val gameState = gameStateRepository.get(payload.gameId)
        val playerErrorNotifier = errorNotifier(outgoingErrorChannelNotification)

        when (this) {
            is IWantToPlayCardCommand -> run(gameState, playerErrorNotifier, eventHandler)
            is IamReadyToPlayCommand -> run(gameState, playerErrorNotifier, eventHandler)
            is IWantToJoinTheGameCommand -> run(gameState, playerErrorNotifier, eventHandler)
            is ICantPlayCommand -> run(gameState, playerErrorNotifier, eventHandler)
        }
    }
}

fun errorNotifier(channel: SendChannel<Notification>): suspend (String) -> Unit =
    {
        val logger = KotlinLogging.logger { }
        val notification = ErrorNotification(message = it)
        logger.atWarn {
            message = "Notification send ERROR: ${notification.message}"
            payload = mapOf("notification" to notification)
        }
        channel.send(notification)
    }
