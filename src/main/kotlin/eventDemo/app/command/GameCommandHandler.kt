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
import eventDemo.libs.command.CommandBlock
import io.ktor.websocket.Frame
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch

/**
 * Listen [GameCommand] on [GameCommandStream], check the validity and execute an action.
 *
 * This action can be executing an action and produce a new [GameEvent] after verification.
 */
class GameCommandHandler(
    private val eventStream: GameEventStream,
) {
    /**
     * Init the handler
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun handle(
        player: Player,
        incoming: ReceiveChannel<Frame>,
        outgoing: SendChannel<Frame>,
    ): Job {
        val commandStream = GameCommandStream(incoming, outgoing)
        val playerNotifier: (String) -> Unit = { outgoing.trySendBlocking(Frame.Text(it)) }
        return GlobalScope.launch {
            init(player, commandStream, playerNotifier)
        }
    }

    private suspend fun init(
        player: Player,
        commandStream: GameCommandStream,
        playerNotifier: (String) -> Unit,
    ) {
        commandStream.process { command ->
            if (command.payload.player.id != player.id) {
                nack()
            }

            val gameState = command.buildGameState()

            when (command) {
                is IWantToPlayCardCommand -> command.run(gameState, playerNotifier, eventStream)
                is IamReadyToPlayCommand -> command.run(gameState, playerNotifier, eventStream)
                is IWantToJoinTheGameCommand -> command.run(gameState, playerNotifier, eventStream)
                is ICantPlayCommand -> command.run(gameState, playerNotifier, eventStream)
            }
        } as CommandBlock<GameCommand>
    }

    private fun GameCommand.buildGameState(): GameState = payload.gameId.buildStateFromEventStream(eventStream)
}
