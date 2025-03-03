package eventDemo.shared.event

import eventDemo.libs.event.Event
import eventDemo.shared.GameId
import eventDemo.shared.entity.Card
import eventDemo.shared.entity.Game

/**
 * An [Event] of a [Game].
 */
sealed interface GameEvent : Event<GameId> {
    override val id: GameId
}

/**
 * An [Event] to represent a played card.
 */
data class CardIsPlayedEvent(
    override val id: GameId,
    val card: Card,
) : GameEvent
