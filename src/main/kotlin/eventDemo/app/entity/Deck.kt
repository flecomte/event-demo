package eventDemo.app.entity

import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val stack: Set<Card> = emptySet(),
    val discard: Set<Card> = emptySet(),
    val playersHands: List<PlayerHand> = emptyList(),
) {
    constructor(players: List<Player>) : this(playersHands = players.map { PlayerHand(it) })

    fun putOneCardOnDiscard(): Deck {
        val takenCard = stack.first()
        val newStack = stack.filterNot { it != takenCard }.toSet()
        return copy(stack = newStack)
    }

    fun take(n: Int): Pair<Deck, List<Card>> {
        val takenCards = stack.take(n)
        val newStack = stack.filterNot { takenCards.contains(it) }.toSet()
        return Pair(copy(stack = newStack), takenCards)
    }

    companion object {
        fun initHands(
            players: Set<Player>,
            handSize: Int = 7,
        ): Deck {
            val deck = new()
            val playersHands = players.map { PlayerHand(it, deck.stack.take(handSize)) }
            val allTakenCards = playersHands.flatMap { it.cards }
            val newStack = deck.stack.filterNot { allTakenCards.contains(it) }.toSet()
            return deck.copy(
                stack = newStack,
                playersHands = playersHands,
            )
        }

        private fun new(): Deck =
            listOf(Card.Color.Red, Card.Color.Blue, Card.Color.Yellow, Card.Color.Green)
                .flatMap { color ->
                    ((0..9) + (1..9)).map { Card.NumericCard(it, color) } +
                        (1..2).map { Card.Plus2Card(color) } +
                        (1..2).map { Card.ReverseCard(color) } +
                        (1..2).map { Card.PassCard(color) }
                }.let {
                    (1..4).map { Card.Plus4Card() }
                }.shuffled()
                .toSet()
                .let { Deck(it) }
    }
}
