package eventDemo.app.command.command

import eventDemo.app.command.ErrorNotifier
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.projection.GameState
import eventDemo.libs.command.CommandId
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
data class IWantToJoinTheGameCommand(
    override val payload: Payload,
) : GameCommand {
    override val id: CommandId = CommandId()

    @Serializable
    data class Payload(
        override val gameId: GameId,
        override val player: Player,
    ) : GameCommand.Payload

    suspend fun run(
        state: GameState,
        playerErrorNotifier: ErrorNotifier,
        eventHandler: GameEventHandler,
    ) {
        if (!state.isStarted) {
            eventHandler.handle(
                NewPlayerEvent(
                    payload.gameId,
                    payload.player,
                ),
            )
        } else {
            playerErrorNotifier("The game is already started")
        }
    }
}
