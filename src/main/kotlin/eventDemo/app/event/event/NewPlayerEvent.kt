package eventDemo.app.event.event

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player

/**
 * An [GameEvent] to represent a new player joining the game.
 */
data class NewPlayerEvent(
    override val id: GameId,
    val player: Player,
) : GameEvent
