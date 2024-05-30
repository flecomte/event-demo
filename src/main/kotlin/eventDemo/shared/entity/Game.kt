package eventDemo.shared.entity

import eventDemo.shared.GameId
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: GameId,
) {
    companion object {
        fun new(): Game = Game(GameId())
    }
}
