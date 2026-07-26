package eventDemo.contexts.game.domain.game

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class DiscardPile(
  val cards: Set<Card> = emptySet(),
) {
  fun withNewCard(card: Card): DiscardPile =
    DiscardPile(cards + card)

  val topCard: Card? get() = cards.lastOrNull()

  val topCardColor: Card.Color? get() = topCard?.let { if (it is Card.CardWithColor) it.color else null }

  val size: Int get() = cards.size
}
