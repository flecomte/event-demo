package eventDemo.app.actions

import eventDemo.app.GameState
import eventDemo.app.command.GameCommand
import eventDemo.app.command.GameCommandStream
import eventDemo.app.command.PlayCardCommand
import eventDemo.app.entity.Player
import eventDemo.app.event.CardIsPlayedEvent
import eventDemo.app.event.GameEvent
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.buildStateFromEventStream
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
            commandStream.process {
                if (it.payload.player.id != player.id) {
                    nack()
                }
                when (it) {
                    is PlayCardCommand -> {
                        // Check the command can be executed
                        val canBeExecuted =
                            it.payload.gameId
                                .buildStateFromEventStream(eventStream)
                                .commandCardCanBeExecuted(it)

                        if (canBeExecuted) {
                            eventStream.publish(
                                CardIsPlayedEvent(
                                    it.payload.gameId,
                                    it.payload.card,
                                    it.payload.player,
                                ),
                            )
                        } else {
                            runBlocking {
                                playerNotifier.send(Frame.Text("Command cannot be executed"))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun GameState.commandCardCanBeExecuted(command: PlayCardCommand): Boolean =
        canBePlayThisCard(
            command.payload.player,
            command.payload.card,
        )
}
