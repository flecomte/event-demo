package eventDemo.app.entity

import eventDemo.shared.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A Play card
 */
@Serializable
sealed interface Card {
    val id: UUID

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

    sealed interface ColorCard : Card {
        val color: Color
    }

    /**
     * A play card with color and number
     */
    @Serializable
    @SerialName("Simple")
    data class NumericCard(
        val number: Int,
        override val color: Color,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : Card,
        ColorCard

    sealed interface Special : Card

    /**
     * A revert card to revert the order of the turn.
     */
    @Serializable
    @SerialName("Reverse")
    data class ReverseCard(
        override val color: Color,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : Special,
        ColorCard

    sealed interface PassTurnCard : Card

    /**
     * A pass card to pass the turn of the next player.
     */
    @Serializable
    @SerialName("Pass")
    data class PassCard(
        override val color: Color,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : Special,
        ColorCard,
        PassTurnCard

    /**
     * A play card to force the next player to take 2 card and pass the turn.
     */
    @Serializable
    @SerialName("Plus2")
    data class Plus2Card(
        override val color: Color,
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : Special,
        ColorCard,
        PassTurnCard

    sealed interface AllColorCard : Card

    /**
     * A play card to force the next player to take 4 card and pass the turn.
     */
    @Serializable
    @SerialName("Plus4")
    class Plus4Card(
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : Special,
        AllColorCard,
        PassTurnCard

    /**
     * A play card to change the color.
     */
    @Serializable
    @SerialName("ChangeColor")
    class ChangeColorCard(
        @Serializable(with = UUIDSerializer::class)
        override val id: UUID = UUID.randomUUID(),
    ) : Special,
        AllColorCard
}
