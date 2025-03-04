package eventDemo.app.event.event

import eventDemo.app.entity.GameId
import eventDemo.libs.event.Event
import kotlinx.serialization.Serializable

/**
 * An [Event] of a Game.
 */
@Serializable
sealed interface GameEvent : Event<GameId> {
    override val id: GameId
}
