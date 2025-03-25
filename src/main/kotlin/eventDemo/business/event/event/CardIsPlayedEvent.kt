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
 * An [GameEvent] to represent a played card.
 */
@Serializable
data class CardIsPlayedEvent(
  override val aggregateId: GameId,
  val card: Card,
  override val player: Player,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  @Serializable(with = UUIDSerializer::class)
  override val eventId: UUID = UUID.randomUUID()
  override val createdAt: Instant = Clock.System.now()
}
