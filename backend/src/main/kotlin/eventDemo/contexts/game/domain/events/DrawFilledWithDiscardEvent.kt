package eventDemo.contexts.game.domain.events

import eventDemo.shared.game.DiscardPile
import eventDemo.shared.game.DrawPile
import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.EventIdSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

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
  override val eventId: EventId = EventId(Uuid.random())
  override val createdAt: Instant = Clock.System.now()
}
