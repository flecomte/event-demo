package eventDemo.app.entity

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PlayersHands(
    private val map: Map<Player, List<Card>> = emptyMap(),
) : Map<Player, List<Card>> by map {
    constructor(players: Set<Player>) : this(players.associateWith { emptyList<Card>() }.toPlayersHands())

    fun removeCard(
        player: Player,
        card: Card,
    ): PlayersHands =
        mapValues { (p, cards) ->
            if (p == player) {
                cards - card
            } else {
                cards
            }
        }.toPlayersHands()

    fun addCards(
        player: Player,
        newCards: List<Card>,
    ): PlayersHands =
        mapValues { (p, cards) ->
            if (p == player) {
                cards + newCards
            } else {
                cards
            }
        }.toPlayersHands()
}

fun Map<Player, List<Card>>.toPlayersHands(): PlayersHands = PlayersHands(this)
