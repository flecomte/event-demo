package eventDemo.app.command

import eventDemo.app.GameId
import eventDemo.app.entity.Card
import eventDemo.app.entity.Player
import eventDemo.libs.command.Command
import eventDemo.libs.command.CommandId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
@SerialName("PlayCard")
data class PlayCardCommand(
    override val payload: Payload,
) : GameCommand {
    constructor(
        gameId: GameId,
        player: Player,
        card: Card,
    ) : this(Payload(gameId, player, card))

    override val name: String = "PlayCard"
    override val id: CommandId = CommandId()

    @Serializable
    data class Payload(
        override val gameId: GameId,
        override val player: Player,
        val card: Card,
    ) : GameCommand.Payload
}

@Serializable
sealed interface GameCommand : Command {
    val payload: Payload

    @Serializable
    sealed interface Payload {
        val gameId: GameId
        val player: Player
    }
}
