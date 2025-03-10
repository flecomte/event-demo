package eventDemo.app.event.event

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import java.util.UUID

/**
 * This [GameEvent] is sent when a player can play.
 */
data class PlayerHavePassEvent(
    override val gameId: GameId,
    override val player: Player,
    val takenCard: Card,
) : GameEvent,
    PlayerActionEvent {
    override val eventId: UUID = UUID.randomUUID()
}
