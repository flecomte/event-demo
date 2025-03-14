package eventDemo.app.event.event

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * This [GameEvent] is sent when a player chose a color.
 */
data class PlayerChoseColorEvent(
  override val aggregateId: GameId,
  override val player: Player,
  val color: Card.Color,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  override val eventId: UUID = UUID.randomUUID()
  override val createdAt: Instant = Clock.System.now()
}
