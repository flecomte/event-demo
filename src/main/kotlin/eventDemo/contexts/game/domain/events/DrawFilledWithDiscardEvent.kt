package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.DiscardPile
import eventDemo.contexts.game.domain.game.DrawPile
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.libs.eventSource.EventId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * When the Pile are shuffled after the draw pille was empty
 */
@Serializable
class DrawFilledWithDiscardEvent(
  override val aggregateId: GameId,
  val newDrawPile: DrawPile,
  val newDiscardPile: DiscardPile,
  override val version: Int,
) : GameEvent {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(UUID.randomUUID())
  override val createdAt: Instant = Clock.System.now()
}
