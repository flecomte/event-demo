package eventDemo.libs.command

import eventDemo.configuration.CommandIdSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An ID for the [Command]
 */
@JvmInline
@Serializable(with = CommandIdSerializer::class)
value class CommandId(
    private val id: UUID = UUID.randomUUID(),
) {
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString(): String = id.toString()
}

/**
 * Interface to represent a Command.
 *
 * A command is a request for an action.
 */
interface Command {
    val id: CommandId
    val name: String
}
