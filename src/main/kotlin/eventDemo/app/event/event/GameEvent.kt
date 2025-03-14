package eventDemo.app.event.event

import eventDemo.app.entity.GameId
import eventDemo.libs.event.Event
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An [Event] of a Game.
 */
@Serializable
sealed interface GameEvent : Event<GameId> {
  override val eventId: UUID
  override val aggregateId: GameId
  override val version: Int
}
