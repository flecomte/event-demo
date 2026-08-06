package eventDemo.shared.game

import eventDemo.shared.serializers.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * A Play card
 */
@Serializable
sealed interface Card {
  val id: Uuid

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

  sealed interface CardWithColor : Card {
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
    override val id: Uuid = Uuid.random(),
  ) : Card,
    CardWithColor {
    init {
      if (number > 9) error("Card number cannot be greater of 9")
      if (number < 0) error("Card number cannot be lower of 0")
    }

    override fun toString(): String =
      "Numeric Card $number $color"
  }

  sealed interface Special : Card

  /**
   * A revert card to revert the order of the turn.
   */
  @Serializable
  @SerialName("Reverse")
  data class ReverseCard(
    override val color: Color,
    @Serializable(with = UUIDSerializer::class)
    override val id: Uuid = Uuid.random(),
  ) : Special,
    CardWithColor {
    override fun toString(): String =
      "Revert Card $color"
  }

  sealed interface PassTurnCard : Card

  /**
   * A pass card to pass the turn of the next player.
   */
  @Serializable
  @SerialName("Pass")
  data class PassCard(
    override val color: Color,
    @Serializable(with = UUIDSerializer::class)
    override val id: Uuid = Uuid.random(),
  ) : Special,
    CardWithColor,
    PassTurnCard {
    override fun toString(): String =
      "Pass Card $color"
  }

  /**
   * A play card to force the next player to take 2 card and pass the turn.
   */
  @Serializable
  @SerialName("Plus2")
  data class Plus2Card(
    override val color: Color,
    @Serializable(with = UUIDSerializer::class)
    override val id: Uuid = Uuid.random(),
  ) : Special,
    CardWithColor,
    PassTurnCard {
    override fun toString(): String =
      "Plus2 Card $color"
  }

  sealed interface CardWith4Color : Card

  /**
   * A play card to force the next player to take 4 card and pass the turn.
   */
  @Serializable
  @SerialName("Plus4")
  class Plus4Card(
    @Serializable(with = UUIDSerializer::class)
    override val id: Uuid = Uuid.random(),
  ) : Special,
    CardWith4Color,
    PassTurnCard {
    override fun toString(): String =
      "Plus4 Card"
  }

  /**
   * A play card to change the color.
   */
  @Serializable
  @SerialName("ChangeColor")
  class ChangeColorCard(
    @Serializable(with = UUIDSerializer::class)
    override val id: Uuid = Uuid.random(),
  ) : Special,
    CardWith4Color {
    override fun toString(): String =
      "Change color Card"
  }
}
