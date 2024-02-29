package eventDemo.app

import eventDemo.plugins.CommandIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
@Serializable(with = CommandIdSerializer::class)
value class CommandId(
    private val id: UUID = UUID.randomUUID(),
) {
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString(): String = id.toString()
}

@Serializable
sealed interface Command {
    val id: CommandId
    val name: String
}

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
