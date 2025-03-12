package eventDemo.app.event.event

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * An [GameEvent] to represent a new player joining the game.
 */
data class NewPlayerEvent(
    override val aggregateId: GameId,
    val player: Player,
    override val version: Int,
) : GameEvent {
    override val eventId: UUID = UUID.randomUUID()
    override val createdAt: Instant = Clock.System.now()
}
