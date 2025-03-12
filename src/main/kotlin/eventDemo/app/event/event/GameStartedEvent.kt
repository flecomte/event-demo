package eventDemo.app.event.event

import eventDemo.app.entity.Deck
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.entity.initHands
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * This [GameEvent] is sent when all players are ready.
 */
data class GameStartedEvent(
    override val aggregateId: GameId,
    val firstPlayer: Player,
    val deck: Deck,
    override val version: Int,
) : GameEvent {
    override val eventId: UUID = UUID.randomUUID()
    override val createdAt: Instant = Clock.System.now()

    companion object {
        fun new(
            id: GameId,
            players: Set<Player>,
            shuffleIsDisabled: Boolean = isDisabled,
            version: Int,
        ): GameStartedEvent =
            GameStartedEvent(
                aggregateId = id,
                firstPlayer = if (shuffleIsDisabled) players.first() else players.random(),
                deck =
                    Deck
                        .newWithoutPlayers()
                        .let { if (shuffleIsDisabled) it else it.shuffle() }
                        .initHands(players)
                        .placeFirstCardOnDiscard(),
                version = version,
            )
    }
}

private var isDisabled = false

internal fun disableShuffleDeck() {
    isDisabled = true
}
