package eventDemo.app.actions

import eventDemo.app.actions.playNewCard.PlayCardCommand
import eventDemo.shared.command.GameCommandStream
import eventDemo.shared.entity.Player
import eventDemo.shared.event.CardIsPlayedEvent
import eventDemo.shared.event.GameEvent
import eventDemo.shared.event.GameEventStream
import eventDemo.shared.event.GameState
import eventDemo.shared.event.buildStateFromEventStream
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Listen [PlayCardCommand] on [GameCommandStream], check the validity and execute the action.
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
}

private fun GameState.commandCardCanBeExecuted(command: PlayCardCommand): Boolean =
    canBePlayThisCard(
        command.payload.player,
        command.payload.card,
    )
