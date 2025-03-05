package eventDemo.app.entity

import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val stack: Stack = emptySet(),
    val discard: Set<Card> = emptySet(),
    val playersHands: PlayerHands = emptyMap(),
) {
    constructor(players: List<Player>) : this(playersHands = players.associateWith { emptyList<Card>() })

    fun placeFirstCardOnDiscard(): Deck {
        val takenCard = stack.first()
        return copy(
            stack = stack - takenCard,
            discard = discard + takenCard,
        )
    }

    fun takeOneCardFromStackTo(player: Player): Deck =
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
                playersHands = playersHands.removeCard(player, card),
            )
        }

    private fun take(n: Int): Pair<Deck, List<Card>> {
        val takenCards = stack.take(n)
        val newStack = stack.filterNot { takenCards.contains(it) }.toSet()
        return Pair(copy(stack = newStack), takenCards)
    }

    private fun takeOne(): Pair<Deck, Card> = take(1).let { (deck, cards) -> Pair(deck, cards.first()) }

    companion object {
        fun newWithoutPlayers(): Deck =
            listOf(Card.Color.Red, Card.Color.Blue, Card.Color.Yellow, Card.Color.Green)
                .flatMap { color ->
                    ((0..9) + (1..9)).map { Card.NumericCard(it, color) } +
                        (1..2).map { Card.Plus2Card(color) } +
                        (1..2).map { Card.ReverseCard(color) } +
                        (1..2).map { Card.PassCard(color) }
                }.let {
                    it + (1..4).map { Card.Plus4Card() }
                }.shuffled()
                .toSet()
                .let { Deck(it) }
    }
}

fun Deck.initHands(
    players: Set<Player>,
    handSize: Int = 7,
): Deck {
    // Copy cards from stack to the player hands
    val deckWithEmptyHands = copy(playersHands = players.associateWith { listOf() })
    return players.fold(deckWithEmptyHands) { acc: Deck, player: Player ->
        val hand = acc.stack.take(handSize)
        val newStack = acc.stack.filterNot { card: Card -> hand.contains(card) }.toSet()
        copy(
            stack = newStack,
            playersHands = acc.playersHands.addCards(player, hand),
        )
    }
}

typealias Stack = Set<Card>
