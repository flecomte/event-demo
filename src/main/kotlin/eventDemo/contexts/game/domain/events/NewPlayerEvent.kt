package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.libs.eventSource.EventId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An [GameEvent] to represent a new player joining the game.
 */
@Serializable
data class NewPlayerEvent(
  override val aggregateId: GameId,
  val player: Player,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  override val playerId: Player.PlayerId get() = player.id

  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(UUID.randomUUID())
  override val createdAt: Instant = Clock.System.now()
}
