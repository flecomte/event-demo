package eventDemo.app.event.event

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import java.util.UUID

/**
 * This [GameEvent] is sent when a player is ready.
 */
data class PlayerWinEvent(
    override val gameId: GameId,
    val player: Player,
) : GameEvent {
    override val eventId: UUID = UUID.randomUUID()
}
