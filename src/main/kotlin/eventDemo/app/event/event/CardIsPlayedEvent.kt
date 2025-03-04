package eventDemo.app.event.event

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player

/**
 * An [GameEvent] to represent a played card.
 */
data class CardIsPlayedEvent(
    override val id: GameId,
    val card: Card,
    val player: Player,
) : GameEvent
