package eventDemo.app

import eventDemo.app.entity.Card
import eventDemo.app.entity.Deck
import eventDemo.app.entity.Player
import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val gameId: GameId,
    val players: Set<Player> = emptySet(),
    val lastPlayer: Player? = null,
    val lastCard: LastCard? = null,
    val lastColor: Card.Color? = null,
    val direction: Direction = Direction.CLOCKWISE,
    val readyPlayers: List<Player> = emptyList(),
    val deck: Deck = Deck(players.toList()),
    val isStarted: Boolean = false,
) {
    @Serializable
    data class LastCard(
        val card: Card,
        val player: Player,
    )

    enum class Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE,
        ;

        fun revert(): Direction =
            if (this === CLOCKWISE) {
                COUNTER_CLOCKWISE
            } else {
                CLOCKWISE
            }
    }

    val isReady: Boolean get() {
        return players.size == readyPlayers.size && players.all { readyPlayers.contains(it) }
    }

    fun canBePlayThisCard(
        player: Player,
        card: Card,
    ): Boolean {
        if (!isReady) return false
        val cardOnGame = lastCard?.card ?: return false

        return when (cardOnGame) {
            is Card.NumericCard -> {
                when (card) {
                    is Card.AllColorCard -> true
                    is Card.NumericCard -> card.number == cardOnGame.number || card.color == cardOnGame.color
                    is Card.ColorCard -> card.color == cardOnGame.color
                }
            }

            is Card.ReverseCard -> {
                when (card) {
                    is Card.ReverseCard -> true
                    is Card.AllColorCard -> true
                    is Card.ColorCard -> card.color == cardOnGame.color
                }
            }

            is Card.PassCard -> {
                if (player.cardOnBoardIsForYou) {
                    false
                } else {
                    when (card) {
                        is Card.AllColorCard -> true
                        is Card.ColorCard -> card.color == cardOnGame.color
                    }
                }
            }

            is Card.ChangeColorCard -> {
                when (card) {
                    is Card.AllColorCard -> true
                    is Card.ColorCard -> card.color == lastColor
                }
            }

            is Card.Plus2Card -> {
                if (player.cardOnBoardIsForYou && card is Card.Plus2Card) {
                    true
                } else {
                    when (card) {
                        is Card.AllColorCard -> true
                        is Card.Plus2Card -> true
                        is Card.ColorCard -> card.color == cardOnGame.color
                    }
                }
            }

            is Card.Plus4Card -> {
                if (player.cardOnBoardIsForYou && card is Card.Plus4Card) {
                    true
                } else {
                    when (card) {
                        is Card.AllColorCard -> true
                        is Card.ColorCard -> card.color == lastColor
                    }
                }
            }
        }
    }

    private val lastPlayerIndex: Int? get() {
        val i = players.indexOf(lastPlayer)
        return if (i == -1) {
            null
        } else {
            i
        }
    }

    private val nextPlayerIndex: Int get() {
        val y =
            if (direction == Direction.CLOCKWISE) {
                +1
            } else {
                -1
            }

        return ((lastPlayerIndex ?: 0) + y) % players.size
    }

    val nextPlayer: Player = players.elementAt(nextPlayerIndex)

    private val Player.currentIndex: Int get() = players.indexOf(this)

    private fun Player.playerDiffIndex(nextPlayer: Player): Int =
        if (direction == Direction.CLOCKWISE) {
            nextPlayer.currentIndex + this.currentIndex
        } else {
            nextPlayer.currentIndex - this.currentIndex
        }.let { it % players.size }

    val Player.cardOnBoardIsForYou: Boolean get() {
        if (lastCard == null) error("No card")
        return this.playerDiffIndex(lastCard.player) == 1
    }
}
