package eventDemo.business.event.projection

import eventDemo.business.entity.Card
import eventDemo.business.entity.Deck
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import kotlinx.serialization.Serializable

@Serializable
data class GameState(
  override val aggregateId: GameId,
  override val lastEventVersion: Int = 0,
  val players: Set<Player> = emptySet(),
  val currentPlayerTurn: Player? = null,
  val lastCardPlayer: Player? = null,
  val colorOnCurrentStack: Card.Color? = null,
  val direction: Direction = Direction.CLOCKWISE,
  val readyPlayers: Set<Player> = emptySet(),
  val deck: Deck = Deck(players),
  val isStarted: Boolean = false,
  val playerWins: Set<Player> = emptySet(),
) : Projection<GameId> {
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

  val cardOnCurrentStack: Card? = deck.discard.lastOrNull()

  val isReady: Boolean get() {
    return players.size == readyPlayers.size && players.all { readyPlayers.contains(it) }
  }

  private val currentPlayerIndex: Int? get() {
    val i = players.indexOf(currentPlayerTurn)
    return if (i == -1) {
      null
    } else {
      i
    }
  }

  private fun nextPlayerIndex(direction: Direction): Int {
    if (players.isEmpty()) return 0

    return if (direction == Direction.CLOCKWISE) {
      sidePlayerIndexClockwise
    } else {
      sidePlayerIndexCounterClockwise
    }
  }

  fun nextPlayer(direction: Direction): Player =
    players.elementAt(nextPlayerIndex(direction))

  private val sidePlayerIndexClockwise: Int by lazy {
    if (players.isEmpty()) {
      0
    } else {
      ((currentPlayerIndex ?: 0) + 1) % players.size
    }
  }
  private val sidePlayerIndexCounterClockwise: Int by lazy {
    if (players.isEmpty()) {
      0
    } else {
      ((currentPlayerIndex ?: 0) - 1) % players.size
    }
  }

  val nextPlayerTurn: Player? by lazy {
    if (players.isEmpty()) {
      null
    } else {
      nextPlayer(direction)
    }
  }

  private val Player.currentIndex: Int get() = players.indexOf(this)

  fun Player.playerDiffIndex(nextPlayer: Player): Int =
    if (direction == Direction.CLOCKWISE) {
      nextPlayer.currentIndex + this.currentIndex
    } else {
      nextPlayer.currentIndex - this.currentIndex
    }.let { it % players.size }

  val Player.cardOnBoardIsForYou: Boolean get() {
    if (lastCardPlayer == null) error("No card")
    return this.playerDiffIndex(lastCardPlayer) == 1
  }

  fun playableCards(player: Player): List<Card> =
    deck
      .playersHands
      .getHand(player)
      ?.filter { canBePlayThisCard(player, it) }
      ?: emptyList()

  fun playerHasNoCardLeft(): List<Player> =
    deck.playerHasNoCardLeft().map { playerId ->
      players.find { it.id == playerId } ?: error("inconsistency detected between players")
    }

  fun canBePlayThisCard(
    player: Player,
    card: Card,
  ): Boolean {
    val cardOnBoard = cardOnCurrentStack ?: return false
    return when (cardOnBoard) {
      is Card.NumericCard -> {
        when (card) {
          is Card.AllColorCard -> true
          is Card.NumericCard -> card.number == cardOnBoard.number || card.color == cardOnBoard.color
          is Card.ColorCard -> card.color == cardOnBoard.color
        }
      }

      is Card.ReverseCard -> {
        when (card) {
          is Card.ReverseCard -> true
          is Card.AllColorCard -> true
          is Card.ColorCard -> card.color == cardOnBoard.color
        }
      }

      is Card.PassCard -> {
        if (player.cardOnBoardIsForYou) {
          false
        } else {
          when (card) {
            is Card.AllColorCard -> true
            is Card.ColorCard -> card.color == cardOnBoard.color
          }
        }
      }

      is Card.ChangeColorCard -> {
        when (card) {
          is Card.AllColorCard -> true
          is Card.ColorCard -> card.color == colorOnCurrentStack
        }
      }

      is Card.Plus2Card -> {
        if (player.cardOnBoardIsForYou && card is Card.Plus2Card) {
          true
        } else {
          when (card) {
            is Card.Plus2Card -> true
            else -> false
          }
        }
      }

      is Card.Plus4Card -> {
        if (player.cardOnBoardIsForYou && card is Card.Plus4Card) {
          true
        } else {
          when (card) {
            is Card.AllColorCard -> true
            is Card.ColorCard -> card.color == colorOnCurrentStack
          }
        }
      }
    }
  }
}
