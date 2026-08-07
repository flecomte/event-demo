package eventDemo.contexts.game.domain.events

import eventDemo.shared.game.DiscardPile
import eventDemo.shared.game.DrawPile
import eventDemo.shared.game.Player
import eventDemo.shared.game.PlayerHand
import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.EventIdSerializer
import eventDemo.shared.serializers.PlayerIdSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * This [GameEvent] is sent when all players are ready.
 */
@Serializable
data class GameStartedEvent(
  override val aggregateId: GameId,
  @Serializable(with = PlayerIdSerializer::class)
  val firstPlayer: Player.PlayerId,
  val playersHans: Map<Player.PlayerId, PlayerHand>,
  val drawPile: DrawPile,
  val discardPile: DiscardPile,
  override val version: Int,
) : GameEvent {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(Uuid.random())
  override val createdAt: Instant = Clock.System.now()
}
