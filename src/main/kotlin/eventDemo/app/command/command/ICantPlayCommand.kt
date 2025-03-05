package eventDemo.app.command.command

import eventDemo.app.GameState
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.PlayerHavePassEvent
import eventDemo.libs.command.CommandId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
@SerialName("Pass")
data class ICantPlayCommand(
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
        val playableCards = state.playableCards(payload.player)
        if (playableCards.isEmpty()) {
            eventStream.publish(
                PlayerHavePassEvent(
                    payload.gameId,
                    payload.player,
                ),
            )
        } else {
            playerNotifier("You can and must play one card, like ${playableCards.first()::class.simpleName}")
        }
    }
}
