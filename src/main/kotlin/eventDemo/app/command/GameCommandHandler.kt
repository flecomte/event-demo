package eventDemo.app.command

import eventDemo.app.GameState
import eventDemo.app.command.command.GameCommand
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.command.command.IwantToPlayCardCommand
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.buildStateFromEventStream
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameEvent
import eventDemo.app.event.event.PlayerReadyEvent
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
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
    private val playerNotifier = outgoing

    /**
     * Init the handler
     */
    fun init(player: Player) {
        CoroutineScope(Dispatchers.IO).launch {
            commandStream.process { command ->
                if (command.payload.player.id != player.id) {
                    nack()
                }

                val state = command.buildState()

                when (command) {
                    is IwantToPlayCardCommand -> {
                        // Check the command can be executed
                        if (state.commandCardCanBeExecuted(command)) {
                            eventStream.publish(
                                CardIsPlayedEvent(
                                    command.payload.gameId,
                                    command.payload.card,
                                    command.payload.player,
                                ),
                            )
                        } else {
                            runBlocking {
                                playerNotifier.send(Frame.Text("Command cannot be executed"))
                            }
                        }
                    }

                    is IamReadyToPlayCommand -> {
                        if (state.playerIsAlreadyReady(command.payload.player)) {
                            nack()
                        } else {
                            PlayerReadyEvent(
                                command.payload.gameId,
                                command.payload.player,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun GameState.playerIsAlreadyReady(player: Player): Boolean = readyPlayers.contains(player)

    private fun GameState.commandCardCanBeExecuted(command: IwantToPlayCardCommand): Boolean =
        canBePlayThisCard(
            command.payload.player,
            command.payload.card,
        )

    private fun GameCommand.buildState(): GameState = payload.gameId.buildStateFromEventStream(eventStream)
}
