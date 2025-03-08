package eventDemo.app.entity

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PlayersHands(
    private val map: Map<Player.PlayerId, List<Card>> = emptyMap(),
) : Map<Player.PlayerId, List<Card>> by map {
    constructor(players: Set<Player>) :
        this(players.map { it.id }.associateWith { emptyList<Card>() }.toPlayersHands())

    fun getHand(player: Player): List<Card>? = this[player.id]

    fun removeCard(
        player: Player,
        card: Card,
    ): PlayersHands =
        mapValues { (playerId, cards) ->
            if (playerId == player.id) {
                if (!cards.contains(card)) error("The hand no contain the card")
                cards - card
            } else {
                cards
            }
        }.toPlayersHands()

    fun addCard(
        player: Player,
        newCard: Card,
    ): PlayersHands = addCards(player, listOf(newCard))

    fun addCards(
        player: Player,
        newCards: List<Card>,
    ): PlayersHands =
        mapValues { (p, cards) ->
            if (p == player.id) {
                if (cards.intersect(newCards).isNotEmpty()) error("The hand already contain the card")
                cards + newCards
            } else {
                cards
            }
        }.toPlayersHands()
}

fun Map<Player.PlayerId, List<Card>>.toPlayersHands(): PlayersHands = PlayersHands(this)
