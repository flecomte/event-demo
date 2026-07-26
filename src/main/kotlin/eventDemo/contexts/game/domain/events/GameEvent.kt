package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.contexts.game.infrastructure.persistence.serializers.GameIdSerializer
import eventDemo.libs.eventSource.Event
import eventDemo.libs.eventSource.EventId
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
