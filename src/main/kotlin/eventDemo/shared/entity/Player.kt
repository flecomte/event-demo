package eventDemo.shared.entity

import eventDemo.libs.event.AggregateId
import eventDemo.plugins.PlayerIdSerializer
import eventDemo.plugins.UUIDSerializer
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

    @JvmInline
    value class PlayerId(
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : AggregateId {
        override fun toString(): String = id.toString()
    }
}

@Serializable
data class PlayerHand(
    val player: Player,
    val cards: List<Card> = emptyList(),
) {
    val count = lazy { cards.count() }
}
