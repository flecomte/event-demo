package eventDemo.app.entity

typealias PlayerHands = Map<Player, List<Card>>

fun PlayerHands.removeCard(
    player: Player,
    card: Card,
): PlayerHands =
    mapValues { (p, cards) ->
        if (p == player) {
            cards - card
        } else {
            cards
        }
    }

fun PlayerHands.addCards(
    player: Player,
    newCards: List<Card>,
): PlayerHands =
    mapValues { (p, cards) ->
        if (p == player) {
            cards + newCards
        } else {
            cards
        }
    }
