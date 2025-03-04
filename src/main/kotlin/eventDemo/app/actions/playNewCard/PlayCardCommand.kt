package eventDemo.app.actions.playNewCard

import eventDemo.libs.command.Command
import eventDemo.libs.command.CommandId
import eventDemo.shared.GameId
import eventDemo.shared.entity.Card
import eventDemo.shared.entity.Player
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
