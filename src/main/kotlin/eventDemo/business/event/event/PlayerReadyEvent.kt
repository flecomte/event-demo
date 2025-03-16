package eventDemo.business.event.event

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * This [GameEvent] is sent when a player is ready.
 */
data class PlayerReadyEvent(
  override val aggregateId: GameId,
  val player: Player,
  override val version: Int,
) : GameEvent {
  override val eventId: UUID = UUID.randomUUID()
  override val createdAt: Instant = Clock.System.now()
}
