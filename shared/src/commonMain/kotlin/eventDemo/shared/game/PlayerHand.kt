package eventDemo.shared.game

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PlayerHand(
  val cards: Set<Card> = emptySet(),
) {
  fun withNewCards(newCards: Set<Card>): PlayerHand =
    PlayerHand(cards + newCards)

  fun withoutTheCards(newCards: Set<Card>): PlayerHand =
    PlayerHand(cards - newCards)

  val size: Int get() =
    cards.size
}
