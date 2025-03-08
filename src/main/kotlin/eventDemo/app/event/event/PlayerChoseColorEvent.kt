package eventDemo.app.event.event

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player

/**
 * This [GameEvent] is sent when a player chose a color.
 */
data class PlayerChoseColorEvent(
    override val gameId: GameId,
    val player: Player,
    val color: Card.Color,
) : GameEvent
