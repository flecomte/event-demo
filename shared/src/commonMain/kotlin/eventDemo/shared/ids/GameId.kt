package eventDemo.shared.ids

import eventDemo.shared.serializers.UUIDSerializer
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * An [AggregateId] for a game.
 */
@JvmInline
@Serializable
value class GameId(
  @Serializable(with = UUIDSerializer::class)
  override val id: Uuid = Uuid.random(),
) : AggregateId {
  override fun toString(): String =
    id.toString()
}
