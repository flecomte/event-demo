package eventDemo.business.event.event

import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.configuration.serializer.UUIDSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * This [GameEvent] is sent when a player chose a color.
 */
@Serializable
data class PlayerChoseColorEvent(
  override val aggregateId: GameId,
  override val player: Player,
  val color: Card.Color,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  @Serializable(with = UUIDSerializer::class)
  override val eventId: UUID = UUID.randomUUID()
  override val createdAt: Instant = Clock.System.now()
}
