package eventDemo.shared

import eventDemo.libs.event.AggregateId
import eventDemo.plugins.GameIdSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An [AggregateId] for a game.
 */
@JvmInline
@Serializable(with = GameIdSerializer::class)
value class GameId(
    override val id: UUID = UUID.randomUUID(),
) : AggregateId
