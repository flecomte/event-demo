package eventDemo.app.entity

import kotlinx.serialization.Serializable

@Serializable
data class PlayerHand(
    val player: Player,
    val cards: List<Card> = emptyList(),
) {
    val count = lazy { cards.count() }
}
