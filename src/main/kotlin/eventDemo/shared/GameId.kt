package eventDemo.shared

import eventDemo.libs.event.AggregateId
import eventDemo.plugins.GameIdSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
@Serializable(with = GameIdSerializer::class)
value class GameId(
    override val id: UUID = UUID.randomUUID(),
) : AggregateId {
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString(): String = id.toString()
}
