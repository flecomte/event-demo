package eventDemo.app.entity

import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val stack: Set<Card> = emptySet(),
    val discard: Set<Card> = emptySet(),
    val playersHands: PlayerHands = emptyMap(),
) {
    constructor(players: List<Player>) : this(playersHands = players.associateWith { emptyList<Card>() })

    fun putOneCardOnDiscard(): Deck {
        val takenCard = stack.first()
        val newStack = stack.filterNot { it != takenCard }.toSet()
        return copy(stack = newStack)
    }

    private fun take(n: Int): Pair<Deck, List<Card>> {
        val takenCards = stack.take(n)
        val newStack = stack.filterNot { takenCards.contains(it) }.toSet()
        return Pair(copy(stack = newStack), takenCards)
    }

    private fun takeOne(): Pair<Deck, Card> = take(1).let { (deck, cards) -> Pair(deck, cards.first()) }

    fun takeOneCardTo(player: Player): Deck =
        takeOne().let { (deck, newPlayerCard) ->
            val newHands =
                deck.playersHands.mapValues { (p, cards) ->
                    if (p == player) {
                        cards + newPlayerCard
                    } else {
                        cards
                    }
                }
            deck.copy(playersHands = newHands)
        }

    fun putOneCardFromHand(
        player: Player,
        card: Card,
    ): Deck =
        run {
            // Validate parameters
            val playerHand =
                playersHands[player]
                    ?: error("No player on this game")
            if (playerHand.none { it == card }) {
                error("No card exist on the player hand")
            }
        }.let {
            copy(
                discard = discard + card,
                playersHands = playersHands.addCard(player, card),
            )
        }

    companion object {
        fun initHands(
            players: Set<Player>,
            handSize: Int = 7,
        ): Deck {
            val deck = new()
            val playersHands = players.associateWith { deck.stack.take(handSize) }
            val allTakenCards = playersHands.flatMap { it.value }
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
