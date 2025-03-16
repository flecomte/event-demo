package eventDemo.business.event.event

import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * This [GameEvent] is sent when a player can play.
 */
data class PlayerHavePassEvent(
  override val aggregateId: GameId,
  override val player: Player,
  val takenCard: Card,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  override val eventId: UUID = UUID.randomUUID()
  override val createdAt: Instant = Clock.System.now()
}
