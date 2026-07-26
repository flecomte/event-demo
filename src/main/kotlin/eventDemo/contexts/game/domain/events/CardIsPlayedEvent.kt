package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.Card
import eventDemo.contexts.game.domain.game.Card.Color
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.libs.eventSource.EventId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

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
  override val eventId: EventId = EventId(UUID.randomUUID())
  override val createdAt: Instant = Clock.System.now()

  val theColorCard get() = if (card is Card.CardWithColor) card.color else chosenColor
}
