package eventDemo.app.command.command

import eventDemo.app.GameState
import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.libs.command.CommandId
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
data class IWantToPlayCardCommand(
    override val payload: Payload,
) : GameCommand {
    override val id: CommandId = CommandId()

    @Serializable
    data class Payload(
        override val gameId: GameId,
        override val player: Player,
        val card: Card,
    ) : GameCommand.Payload

    fun run(
        state: GameState,
        playerErrorNotifier: (String) -> Unit,
        eventStream: GameEventStream,
    ) {
        if (!state.isStarted) {
            playerErrorNotifier("The game is Not started")
            return
        }

        if (state.canBePlayThisCard(payload.player, payload.card)) {
            eventStream.publish(
                CardIsPlayedEvent(
                    payload.gameId,
                    payload.card,
                    payload.player,
                ),
            )
        } else {
            playerErrorNotifier("You cannot play this card")
        }
    }
}
