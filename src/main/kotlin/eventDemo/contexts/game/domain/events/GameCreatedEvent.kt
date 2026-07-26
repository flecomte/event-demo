package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.libs.eventSource.EventId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * This [GameEvent] is sent when all players are ready.
 */
@Serializable
data class GameCreatedEvent(
  override val aggregateId: GameId,
  override val version: Int,
) : GameEvent {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(UUID.randomUUID())
  override val createdAt: Instant = Clock.System.now()
}
