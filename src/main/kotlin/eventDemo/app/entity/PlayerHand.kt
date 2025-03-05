package eventDemo.app.entity

typealias PlayerHands = Map<Player, List<Card>>

fun PlayerHands.removeCard(
    player: Player,
    card: Card,
) = mapValues { (p, cards) ->
    if (p == player) {
        cards - card
    } else {
        cards
    }
}

fun PlayerHands.addCards(
    player: Player,
    newCards: List<Card>,
) = mapValues { (p, cards) ->
    if (p == player) {
        cards + newCards
    } else {
        cards
    }
}
