package eventDemo.app.command.command

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.projection.GameState
import eventDemo.libs.command.CommandId
import io.github.oshai.kotlinlogging.KotlinLogging
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
        playerErrorNotifier: suspend (String) -> Unit,
        eventHandler: GameEventHandler,
    ) {
        val logger = KotlinLogging.logger {}
        if (!state.isStarted) {
            eventHandler.handle(
                NewPlayerEvent(
                    payload.gameId,
                    payload.player,
                ),
            )
        } else {
            logger.atWarn {
                message = "The game is already started"
                payload = mapOf("player" to this@IWantToJoinTheGameCommand.payload.player)
            }
            playerErrorNotifier("The game is already started")
        }
    }
}
