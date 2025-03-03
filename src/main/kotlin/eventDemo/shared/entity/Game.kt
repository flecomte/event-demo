package eventDemo.shared.entity

import eventDemo.shared.GameId
import kotlinx.serialization.Serializable

/**
 * Represent a Game
 */
@Serializable
data class Game(
    val id: GameId,
) {
    companion object {
        fun new(): Game = Game(GameId())
    }
}
