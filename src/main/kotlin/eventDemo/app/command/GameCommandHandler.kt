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
import eventDemo.shared.toFrame
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

/**
 * Listen [GameCommand] on [GameCommandStream], check the validity and execute an action.
 *
 * This action can be executing an action and produce a new [GameEvent] after verification.
 */
class GameCommandHandler(
    private val eventHandler: GameEventHandler,
    private val gameStateRepository: GameStateRepository,
) {
    private val logger = KotlinLogging.logger { }

    /**
     * Init the handler
     */
    suspend fun handle(
        player: Player,
        incoming: ReceiveChannel<Frame>,
        outgoing: SendChannel<Frame>,
    ) {
        val commandStream = GameCommandStream(incoming)
        val playerErrorNotifier: (String) -> Unit = {
            val notification = ErrorNotification(message = it)
            logger.atInfo {
                message = "Notification send ERROR: ${notification.message}"
                payload = mapOf("notification" to notification)
            }
            outgoing.trySendBlocking(notification.toFrame())
        }
        return init(player, commandStream, playerErrorNotifier)
    }

    private suspend fun init(
        player: Player,
        commandStream: GameCommandStream,
        playerErrorNotifier: (String) -> Unit,
    ) {
        commandStream.process { command ->
            if (command.payload.player.id != player.id) {
                nack()
            }

            val gameState = gameStateRepository.get(command.payload.gameId)

            when (command) {
                is IWantToPlayCardCommand -> command.run(gameState, playerErrorNotifier, eventHandler)
                is IamReadyToPlayCommand -> command.run(gameState, playerErrorNotifier, eventHandler)
                is IWantToJoinTheGameCommand -> command.run(gameState, playerErrorNotifier, eventHandler)
                is ICantPlayCommand -> command.run(gameState, playerErrorNotifier, eventHandler)
            }
        }
    }
}
