package eventDemo.app.command.command

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.libs.command.CommandId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
@SerialName("PlayCard")
data class IwantToPlayCardCommand(
    override val payload: Payload,
) : GameCommand {
    override val name: String = "PlayCard"
    override val id: CommandId = CommandId()

    @Serializable
    data class Payload(
        override val gameId: GameId,
        override val player: Player,
        val card: Card,
    ) : GameCommand.Payload
}
