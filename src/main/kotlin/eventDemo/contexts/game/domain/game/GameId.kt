package eventDemo.contexts.game.domain.game

import eventDemo.libs.eventSource.AggregateId
import eventDemo.libs.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An [AggregateId] for a game.
 */
@JvmInline
@Serializable
value class GameId(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
) : AggregateId {
  override fun toString(): String =
    id.toString()
}
