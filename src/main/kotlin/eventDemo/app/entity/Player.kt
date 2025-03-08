package eventDemo.app.entity

import eventDemo.libs.event.AggregateId
import eventDemo.shared.PlayerIdSerializer
import eventDemo.shared.UUIDSerializer
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Player(
    val name: String,
    @Serializable(with = PlayerIdSerializer::class)
    val id: PlayerId = PlayerId(UUID.randomUUID()),
) : Principal {
    constructor(id: String, name: String) : this(
        name,
        PlayerId(UUID.fromString(id)),
    )

    @Serializable
    @JvmInline
    value class PlayerId(
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : AggregateId {
        override fun toString(): String = id.toString()
    }
}
