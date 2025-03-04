package eventDemo.app.command.command

import eventDemo.app.GameState
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.libs.command.CommandId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A command to set as ready to play
 */
@Serializable
@SerialName("Ready")
data class IamReadyToPlayCommand(
    override val payload: Payload,
) : GameCommand {
    override val id: CommandId = CommandId()

    @Serializable
    data class Payload(
        override val gameId: GameId,
        override val player: Player,
    ) : GameCommand.Payload

    fun run(
        state: GameState,
        playerNotifier: (String) -> Unit,
        eventStream: GameEventStream,
    ) {
        val playerIsAlreadyReady: Boolean = state.readyPlayers.contains(payload.player)

        if (playerIsAlreadyReady) {
            playerNotifier("You are already ready")
        } else {
            eventStream.publish(
                PlayerReadyEvent(
                    payload.gameId,
                    payload.player,
                ),
            )
        }
    }
}
