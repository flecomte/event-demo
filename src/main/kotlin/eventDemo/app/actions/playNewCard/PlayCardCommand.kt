package eventDemo.app.actions.playNewCard

import eventDemo.libs.command.Command
import eventDemo.libs.command.CommandId
import eventDemo.shared.entity.Card
import eventDemo.shared.entity.Game
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PlayCard")
data class PlayCardCommand(
    val payload: Payload,
) : Command {
    constructor(
        game: Game,
        card: Card,
    ) : this(Payload(game, card))

    override val name: String = "PlayCard"
    override val id: CommandId = CommandId()

    @Serializable
    data class Payload(
        val game: Game,
        val card: Card,
    )
}
