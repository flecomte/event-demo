package eventDemo.app.command

import eventDemo.app.GameState
import eventDemo.app.command.command.GameCommand
import eventDemo.app.command.command.ICantPlayCommand
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.command.command.IWantToPlayCardCommand
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.buildStateFromEventStream
import eventDemo.app.event.event.GameEvent
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
    private val eventStream: GameEventStream,
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

            val gameState = command.buildGameState()

            when (command) {
                is IWantToPlayCardCommand -> command.run(gameState, playerErrorNotifier, eventStream)
                is IamReadyToPlayCommand -> command.run(gameState, playerErrorNotifier, eventStream)
                is IWantToJoinTheGameCommand -> command.run(gameState, playerErrorNotifier, eventStream)
                is ICantPlayCommand -> command.run(gameState, playerErrorNotifier, eventStream)
            }
        }
    }

    private fun GameCommand.buildGameState(): GameState = payload.gameId.buildStateFromEventStream(eventStream)
}
