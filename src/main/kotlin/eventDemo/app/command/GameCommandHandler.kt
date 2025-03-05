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
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

/**
 * Listen [GameCommand] on [GameCommandStream], check the validity and execute an action.
 *
 * This action can be executing an action and produce a new [GameEvent] after verification.
 */
class GameCommandHandler(
    private val eventStream: GameEventStream,
    incoming: ReceiveChannel<Frame>,
    outgoing: SendChannel<Frame>,
) {
    private val commandStream = GameCommandStream(incoming, outgoing)
    private val playerNotifier: (String) -> Unit = { runBlocking { outgoing.send(Frame.Text(it)) } }

    /**
     * Init the handler
     */
    suspend fun init(player: Player) {
        commandStream.process { command ->
            if (command.payload.player.id != player.id) {
                runBlocking {
                    nack()
                }
            }

            val gameState = command.buildGameState()

            when (command) {
                is IWantToPlayCardCommand -> command.run(gameState, playerNotifier, eventStream)
                is IamReadyToPlayCommand -> command.run(gameState, playerNotifier, eventStream)
                is IWantToJoinTheGameCommand -> command.run(gameState, playerNotifier, eventStream)
                is ICantPlayCommand -> command.run(gameState, playerNotifier, eventStream)
            }
        }
    }

    private fun GameCommand.buildGameState(): GameState = payload.gameId.buildStateFromEventStream(eventStream)
}
