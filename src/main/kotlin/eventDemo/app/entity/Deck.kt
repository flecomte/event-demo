package eventDemo.app.entity

import kotlinx.serialization.Serializable

@Serializable
data class Deck(
    val stack: Stack = Stack(),
    val discard: Discard = Discard(),
    val playersHands: PlayersHands = PlayersHands(),
) {
    constructor(players: Set<Player>) :
        this(playersHands = PlayersHands(players))

    fun shuffle(): Deck = copy(stack = stack.shuffle())

    fun placeFirstCardOnDiscard(): Deck {
        val takenCard = stack.first()
        return copy(
            stack = stack - takenCard,
            discard = discard + takenCard,
        )
    }

    fun takeOneCardFromStackTo(player: Player): Deck =
        takeOne().let { (deck, newPlayerCard) ->
            deck.copy(
                playersHands = deck.playersHands.addCard(player, newPlayerCard),
            )
        }

    fun putOneCardFromHand(
        player: Player,
        card: Card,
    ): Deck =
        run {
            // Validate parameters
            val playerHand =
                playersHands.getHand(player)
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

    fun playerHasNoCardLeft(): List<Player.PlayerId> =
        playersHands
            .filter { (playerId, hand) -> hand.isEmpty() }
            .map { (playerId, hand) -> playerId }

    private fun take(n: Int): Pair<Deck, List<Card>> {
        val takenCards = stack.take(n)
        val newStack = stack.filterNot { takenCards.contains(it) }.toStack()
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
                }.toStack()
                .let { Deck(it) }
    }
}

fun Deck.initHands(
    players: Set<Player>,
    handSize: Int = 7,
): Deck {
    // Copy cards from stack to the player hands
    val deckWithEmptyHands = copy(playersHands = PlayersHands(players))
    return players.fold(deckWithEmptyHands) { acc: Deck, player: Player ->
        val hand = acc.stack.take(handSize)
        val newStack = acc.stack.filterNot { card: Card -> hand.contains(card) }.toStack()
        copy(
            stack = newStack,
            playersHands = acc.playersHands.addCards(player, hand),
        )
    }
}

@JvmInline
@Serializable
value class Stack(
    private val cards: Set<Card> = emptySet(),
) : Set<Card> by cards {
    operator fun plus(card: Card): Stack = cards.plus(card).toStack()

    operator fun minus(card: Card): Stack = cards.minus(card).toStack()

    fun shuffle(): Stack = shuffled().toStack()
}

fun List<Card>.toStack(): Stack = Stack(this.toSet())

fun Set<Card>.toStack(): Stack = Stack(this)

@JvmInline
@Serializable
value class Discard(
    private val cards: Set<Card> = emptySet(),
) : Set<Card> by cards {
    operator fun plus(card: Card): Discard = cards.plus(card).toDiscard()

    operator fun minus(card: Card): Discard = cards.minus(card).toDiscard()
}

fun List<Card>.toDiscard(): Discard = Discard(this.toSet())

fun Set<Card>.toDiscard(): Discard = Discard(this)
