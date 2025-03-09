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
            shuffleIsDisabled: Boolean = isDisabled,
        ): GameStartedEvent =
            GameStartedEvent(
                gameId = id,
                firstPlayer = if (shuffleIsDisabled) players.first() else players.random(),
                deck =
                    Deck
                        .newWithoutPlayers()
                        .let { if (shuffleIsDisabled) it else it.shuffle() }
                        .initHands(players)
                        .placeFirstCardOnDiscard(),
            )
    }
}

private var isDisabled = false

internal fun disableShuffleDeck() {
    isDisabled = true
}
