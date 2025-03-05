package eventDemo.app.entity

typealias PlayerHands = Map<Player, List<Card>>

fun PlayerHands.addCard(
    player: Player,
    card: Card,
) = mapValues { (p, cards) ->
    if (p == player) {
        cards - card
    } else {
        cards
    }
}
