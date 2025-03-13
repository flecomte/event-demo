package eventDemo.app.command.command

import eventDemo.app.command.ErrorNotifier
import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.projection.GameState
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
        override val aggregateId: GameId,
        override val player: Player,
        val card: Card,
    ) : GameCommand.Payload

    suspend fun run(
        state: GameState,
        playerErrorNotifier: ErrorNotifier,
        eventHandler: GameEventHandler,
    ) {
        if (!state.isStarted) {
            playerErrorNotifier("The game is Not started")
            return
        }
        if (state.currentPlayerTurn != payload.player) {
            playerErrorNotifier("Its not your turn!")
            return
        }

        if (state.canBePlayThisCard(payload.player, payload.card)) {
            eventHandler.handle(payload.aggregateId) {
                CardIsPlayedEvent(
                    aggregateId = payload.aggregateId,
                    card = payload.card,
                    player = payload.player,
                    version = it,
                )
            }
        } else {
            playerErrorNotifier("You cannot play this card")
        }
    }
}
