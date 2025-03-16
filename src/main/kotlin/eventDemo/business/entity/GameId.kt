package eventDemo.business.entity

import eventDemo.configuration.GameIdSerializer
import eventDemo.libs.event.AggregateId
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An [AggregateId] for a game.
 */
@JvmInline
@Serializable(with = GameIdSerializer::class)
value class GameId(
  override val id: UUID = UUID.randomUUID(),
) : AggregateId {
  override fun toString(): String =
    id.toString()
}
