package eventDemo.contexts.game.domain.events

import eventDemo.libs.eventSource.Event
import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.EventIdSerializer
import eventDemo.shared.serializers.GameIdSerializer
import kotlinx.serialization.Serializable

/**
 * An [Event] of a Game.
 */
@Serializable
sealed interface GameEvent : Event<GameId> {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId

  @Serializable(with = GameIdSerializer::class)
  override val aggregateId: GameId
  override val version: Int
}
