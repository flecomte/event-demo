package eventDemo.app.event.event

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player

/**
 * This [GameEvent] is sent when a player is ready.
 */
data class PlayerReadyEvent(
    override val gameId: GameId,
    val player: Player,
) : GameEvent
