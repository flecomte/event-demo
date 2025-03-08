package eventDemo.app.event.event

import eventDemo.app.entity.Deck
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.entity.initHands

/**
 * This [GameEvent] is sent when all players are ready.
 */
data class GameStartedEvent(
    override val gameId: GameId,
    val firstPlayer: Player,
    val deck: Deck,
) : GameEvent {
    companion object {
        fun new(
            id: GameId,
            players: Set<Player>,
        ): GameStartedEvent =
            GameStartedEvent(
                gameId = id,
                firstPlayer = if (isDisabled) players.first() else players.random(),
                deck =
                    Deck
                        .newWithoutPlayers()
                        .let { if (isDisabled) it else it.shuffle() }
                        .initHands(players)
                        .placeFirstCardOnDiscard(),
            )
    }
}

private var isDisabled = false

internal fun disableShuffleDeck() {
    isDisabled = true
}
