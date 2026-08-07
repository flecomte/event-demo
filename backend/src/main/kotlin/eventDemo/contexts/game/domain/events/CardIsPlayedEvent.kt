package eventDemo.contexts.game.domain.events

import eventDemo.shared.game.Card
import eventDemo.shared.game.Card.Color
import eventDemo.shared.game.Player
import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.EventIdSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * An [GameEvent] to represent a played card.
 */
@Serializable
data class CardIsPlayedEvent(
  override val aggregateId: GameId,
  val card: Card,
  override val playerId: Player.PlayerId,
  val chosenColor: Color? = null,
  override val version: Int,
) : GameEvent,
  PlayerActionEvent {
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(Uuid.random())
  override val createdAt: Instant = Clock.System.now()

  val theColorCard get() = if (card is Card.CardWithColor) card.color else chosenColor
}
