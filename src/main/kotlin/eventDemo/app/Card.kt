package eventDemo.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: GameId,
) {
    companion object {
        fun new(): Game {
            return Game(GameId())
        }
    }
}

@Serializable
sealed interface Card {
    @Serializable
    enum class Color {
        Blue,
        Red,
        Yellow,
        Green,
    }

    @Serializable
    @SerialName("Simple")
    data class Simple(
        val number: Int,
        val color: Color,
    ) : Card

    sealed interface Special : Card

    @Serializable
    @SerialName("Reverse")
    data class ReverseCard(
        val color: Color,
    ) : Special
}
