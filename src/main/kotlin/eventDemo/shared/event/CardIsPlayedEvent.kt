package eventDemo.shared.event

import eventDemo.libs.event.Event
import eventDemo.shared.GameId
import eventDemo.shared.entity.Card

sealed interface GameEvent : Event<GameId> {
    override val id: GameId
}

data class CardIsPlayedEvent(
    override val id: GameId,
    val card: Card,
) : GameEvent
