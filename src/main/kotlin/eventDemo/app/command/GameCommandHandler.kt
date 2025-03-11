package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.app.entity.Player
import eventDemo.app.event.event.GameEvent
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
    private val commandStreamChannel: CommandStreamChannelBuilder<GameCommand>,
    private val runner: GameCommandRunner,
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
                runner.run(command, outgoingErrorChannelNotification)
            }
        }
}
