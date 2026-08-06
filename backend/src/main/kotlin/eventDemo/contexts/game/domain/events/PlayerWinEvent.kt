package eventDemo.contexts.game.domain.events

import eventDemo.shared.game.Player
import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.EventIdSerializer
import eventDemo.shared.serializers.PlayerIdSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * This [GameEvent] is sent when a player is ready.
 */
@Serializable
data class PlayerWinEvent(
  override val aggregateId: GameId,
  @Serializable(with = PlayerIdSerializer::class)
  override val playerId: Player.PlayerId,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(Uuid.random())
  override val createdAt: Instant = Clock.System.now()
}
