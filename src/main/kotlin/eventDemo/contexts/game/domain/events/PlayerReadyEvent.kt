package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.contexts.game.infrastructure.persistence.serializers.PlayerIdSerializer
import eventDemo.libs.eventSource.EventId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * This [GameEvent] is sent when a player is ready.
 */
@Serializable
data class PlayerReadyEvent(
  override val aggregateId: GameId,
  @Serializable(with = PlayerIdSerializer::class)
  override val playerId: Player.PlayerId,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(UUID.randomUUID())
  override val createdAt: Instant = Clock.System.now()
}
