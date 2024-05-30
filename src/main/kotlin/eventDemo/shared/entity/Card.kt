package eventDemo.shared.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    data class NumericCard(
        val number: Int,
        val color: Color,
    ) : Card

    sealed interface Special : Card

    @Serializable
    @SerialName("Reverse")
    data class ReverseCard(
        val color: Color,
    ) : Special

    @Serializable
    @SerialName("Pass")
    data class PassCard(
        val color: Color,
    ) : Special

    @Serializable
    @SerialName("Plus2")
    data class Plus2Card(
        val color: Color,
    ) : Special

    @Serializable
    @SerialName("Plus4")
    data class Plus4Card(
        val nextColor: Color,
    ) : Special

    @Serializable
    @SerialName("ChangeColor")
    data class ChangeColorCard(
        val nextColor: Color,
    ) : Special
}
