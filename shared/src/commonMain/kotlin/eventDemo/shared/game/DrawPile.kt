package eventDemo.shared.game

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class DrawPile(
  val cards: Set<Card> = emptySet(),
) {
  val size: Int get() = cards.size

  fun take(number: Int): Pair<DrawPile, Set<Card>> =
    cards.drop(number).toDrawPile() to cards.take(number).toSet()

  val remainingCards
    get() = cards.size

  fun shuffled(): DrawPile =
    cards.shuffled().toDrawPile()

  val firstCard get() = cards.first()

  fun generateValidDrawPile(): DrawPile =
    if (cards.first() is Card.CardWith4Color) {
      DrawPile(setOf(cards.first()) + cards.drop(1))
        .generateValidDrawPile()
    } else {
      this
    }
}

private fun List<Card>.toDrawPile(): DrawPile =
  DrawPile(this.toSet())
