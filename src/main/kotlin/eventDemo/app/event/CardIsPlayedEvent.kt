package eventDemo.app.event

import eventDemo.app.GameId
import eventDemo.app.entity.Card
import eventDemo.app.entity.Deck
import eventDemo.app.entity.Player
import eventDemo.libs.event.Event
import kotlinx.serialization.Serializable

/**
 * An [Event] of a Game.
 */
@Serializable
sealed interface GameEvent : Event<GameId> {
    override val id: GameId
}

/**
 * An [Event] to represent a played card.
 */
data class CardIsPlayedEvent(
    override val id: GameId,
    val card: Card,
    val player: Player,
) : GameEvent

/**
 * An [Event] to represent a new player joining the game.
 */
data class NewPlayerEvent(
    override val id: GameId,
    val player: Player,
) : GameEvent

/**
 * This [Event] is sent when a player is ready.
 */
data class PlayerReadyEvent(
    override val id: GameId,
    val player: Player,
) : GameEvent

/**
 * This [Event] is sent when a player is ready.
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

/**
 * This [Event] is sent when a player can play.
 */
data class PlayerHavePassEvent(
    override val id: GameId,
    val player: Player,
) : GameEvent

/**
 * This [Event] is sent when a player chose a color.
 */
data class PlayerChoseColorEvent(
    override val id: GameId,
    val player: Player,
    val color: Card.Color,
) : GameEvent
