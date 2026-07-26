package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.DiscardPile
import eventDemo.contexts.game.domain.game.DrawPile
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.domain.game.PlayerHand
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.contexts.game.infrastructure.persistence.serializers.PlayerIdSerializer
import eventDemo.libs.eventSource.EventId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

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
  override val eventId: EventId = EventId(UUID.randomUUID())
  override val createdAt: Instant = Clock.System.now()
}
