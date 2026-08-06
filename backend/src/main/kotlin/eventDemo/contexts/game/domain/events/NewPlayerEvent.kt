package eventDemo.contexts.game.domain.events

import eventDemo.shared.game.Player
import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.EventIdSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

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
  override val eventId: EventId = EventId(Uuid.random())
  override val createdAt: Instant = Clock.System.now()
}
