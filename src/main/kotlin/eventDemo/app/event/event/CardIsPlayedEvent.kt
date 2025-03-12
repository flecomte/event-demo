package eventDemo.app.event.event

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * An [GameEvent] to represent a played card.
 */
data class CardIsPlayedEvent(
    override val aggregateId: GameId,
    val card: Card,
    override val player: Player,
    override val version: Int,
) : GameEvent,
    PlayerActionEvent {
    override val eventId: UUID = UUID.randomUUID()
    override val createdAt: Instant = Clock.System.now()
}
