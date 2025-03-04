package eventDemo.app.event.event

import eventDemo.app.entity.Deck
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player

/**
 * This [GameEvent] is sent when all players is ready.
 */
data class GameStartedEvent(
    override val id: GameId,
    val firstPlayer: Player,
    val deck: Deck,
) : GameEvent {
    companion object {
        fun new(
            id: GameId,
            players: Set<Player>,
        ): GameStartedEvent =
            GameStartedEvent(
                id = id,
                firstPlayer = players.random(),
                deck = Deck.initHands(players).putOneCardOnDiscard(),
            )
    }
}
