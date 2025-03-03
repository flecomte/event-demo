package eventDemo.shared.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A Play card
 */
@Serializable
sealed interface Card {
    /**
     * The color of a card
     */
    @Serializable
    enum class Color {
        Blue,
        Red,
        Yellow,
        Green,
    }

    /**
     * A play card with color and number
     */
    @Serializable
    @SerialName("Simple")
    data class NumericCard(
        val number: Int,
        val color: Color,
    ) : Card

    sealed interface Special : Card

    /**
     * A revert card to revert the order of the turn.
     */
    @Serializable
    @SerialName("Reverse")
    data class ReverseCard(
        val color: Color,
    ) : Special

    /**
     * A pass card to pass the turn of the next player.
     */
    @Serializable
    @SerialName("Pass")
    data class PassCard(
        val color: Color,
    ) : Special

    /**
     * A play card to force the next player to take 2 card and pass the turn.
     */
    @Serializable
    @SerialName("Plus2")
    data class Plus2Card(
        val color: Color,
    ) : Special

    /**
     * A play card to force the next player to take 4 card and pass the turn.
     */
    @Serializable
    @SerialName("Plus4")
    data class Plus4Card(
        val nextColor: Color,
    ) : Special

    /**
     * A play card to change the color.
     */
    @Serializable
    @SerialName("ChangeColor")
    data class ChangeColorCard(
        val nextColor: Color,
    ) : Special
}
