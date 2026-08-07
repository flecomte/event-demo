package eventDemo.contexts.game.domain.events

import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.EventIdSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * This [GameEvent] is sent when all players are ready.
 */
@Serializable
data class GameCreatedEvent(
  override val aggregateId: GameId,
  override val version: Int,
) : GameEvent {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(Uuid.random())
  override val createdAt: Instant = Clock.System.now()
}
