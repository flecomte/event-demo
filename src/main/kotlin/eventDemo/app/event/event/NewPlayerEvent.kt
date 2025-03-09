package eventDemo.app.event.event

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import java.util.UUID

/**
 * An [GameEvent] to represent a new player joining the game.
 */
data class NewPlayerEvent(
    override val gameId: GameId,
    val player: Player,
) : GameEvent {
    override val eventId: UUID = UUID.randomUUID()
}
