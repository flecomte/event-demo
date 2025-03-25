package eventDemo.business.event.event

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.configuration.serializer.UUIDSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An [GameEvent] to represent a new player joining the game.
 */
@Serializable
data class NewPlayerEvent(
  override val aggregateId: GameId,
  val player: Player,
  override val version: Int,
) : GameEvent {
  @Serializable(with = UUIDSerializer::class)
  override val eventId: UUID = UUID.randomUUID()
  override val createdAt: Instant = Clock.System.now()
}
