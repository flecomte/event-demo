package eventDemo.contexts.game.domain.game.gameState

import eventDemo.contexts.game.domain.events.CardIsPlayedEvent
import eventDemo.contexts.game.domain.events.DrawFilledWithDiscardEvent
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.events.PlayerActionEvent
import eventDemo.contexts.game.domain.events.PlayerHaveDrawCardEvent
import eventDemo.contexts.game.domain.events.PlayerWinEvent
import eventDemo.contexts.game.domain.game.Card
import eventDemo.contexts.game.domain.game.Card.Color
import eventDemo.contexts.game.domain.game.DiscardPile
import eventDemo.contexts.game.domain.game.DrawPile
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.domain.game.PlayerList
import eventDemo.contexts.game.domain.game.errors.InconsistentGameException
import eventDemo.contexts.game.domain.game.errors.ItsNotTheTurnException
import eventDemo.contexts.game.domain.game.errors.TheCardHasNoColorException
import eventDemo.contexts.game.domain.game.errors.TheCardIsAColorCardException
import eventDemo.contexts.game.domain.game.errors.ThePlayerHasAlreadyWinException
import eventDemo.contexts.game.domain.game.errors.ThePlayerHasRemainingCardsException
import eventDemo.contexts.game.domain.game.errors.ThePlayerIsNotInTheGameException
import eventDemo.contexts.game.domain.game.errors.ThePlayerMustPlayACardException
import eventDemo.contexts.game.domain.game.gameState.Game.Direction

fun PlayerList.nextPlayerTurn(
  lastPlayerId: Player.PlayerId,
  direction: Direction,
): Player.PlayerId {
  val lastPlayer = get(lastPlayerId)
  val playersLastTurn = filter { it.hand.cards.isNotEmpty() || it == lastPlayer }

  return playersLastTurn
    .indexOf(lastPlayer)
    .let { lastPlayerIndex ->
      if (direction == Direction.CLOCKWISE) {
        if (lastPlayerIndex == playersLastTurn.size - 1) {
          0
        } else {
          lastPlayerIndex + 1
        }
      } else {
        if (lastPlayerIndex == 0) {
          playersLastTurn.size - 1
        } else {
          lastPlayerIndex - 1
        }
      }
    }.let { nextPlayerIndex -> elementAt(nextPlayerIndex).id }
}

data class GameStarted(
  override val aggregateId: GameId,
  override val players: PlayerList,
  val drawPile: DrawPile,
  val discardPile: DiscardPile,
  val lastPlayerId: Player.PlayerId?,
  val nextPlayerId: Player.PlayerId,
  val currentColor: Color,
  val playedTurnHistory: List<History> = emptyList(),
  val direction: Direction = Direction.CLOCKWISE,
  val playerWins: Set<Player.PlayerId> = emptySet(),
  override val version: Int,
  override val recordedEvents: Set<GameEvent>,
) : Game {
  val playersInGame by lazy { players.filter { it.hand.cards.isNotEmpty() } }

  val lastPlayedCard: Card? by lazy { discardPile.topCard }

  data class History(
    val playerId: Player.PlayerId,
    val event: GameEvent,
    val direction: Direction,
  )

  val lastPlayer: Player? by lazy { lastPlayerId?.let { players.get(it) } }

  val nextPlayer: Player by lazy { players.get(nextPlayerId) }

  fun canBePlayThisCard(card: Card): Boolean {
    val cardOnBoard = discardPile.topCard ?: return false
    return when (cardOnBoard) {
      is Card.NumericCard -> {
        when (card) {
          is Card.CardWith4Color -> true
          is Card.NumericCard -> card.number == cardOnBoard.number || card.color == cardOnBoard.color
          is Card.CardWithColor -> card.color == cardOnBoard.color
        }
      }

      is Card.ReverseCard -> {
        when (card) {
          is Card.ReverseCard -> true
          is Card.CardWith4Color -> true
          is Card.CardWithColor -> card.color == cardOnBoard.color
        }
      }

      is Card.PassCard -> {
        when (card) {
          is Card.CardWith4Color -> true
          is Card.CardWithColor -> card.color == cardOnBoard.color
        }
      }

      is Card.ChangeColorCard -> {
        when (card) {
          is Card.CardWith4Color -> true
          is Card.CardWithColor -> card.color == currentColor
        }
      }

      is Card.Plus2Card -> {
        when (card) {
          is Card.Plus2Card -> true
          ::isPlayedLastTurn -> false
          is Card.CardWith4Color -> true
          is Card.CardWithColor -> card.color == currentColor
        }
      }

      is Card.Plus4Card -> {
        when (card) {
          is Card.Plus4Card -> true
          ::isPlayedLastTurn -> false
          is Card.CardWith4Color -> true
          is Card.CardWithColor -> card.color == currentColor
        }
      }
    }
  }

  fun playableCards(playerId: Player.PlayerId): Set<Card> =
    players
      .get(playerId)
      .hand
      .cards
      .filter(::canBePlayThisCard)
      .toSet()

  fun playTheCard(
    playerId: Player.PlayerId,
    card: Card,
    chosenColor: Color? = null,
  ): GameStarted =
    CardIsPlayedEvent(aggregateId, card, playerId, chosenColor, version + 1)
      .checkPlayerTurn()
      .checkState({
        (card is Card.CardWithColor && chosenColor == null) || card is Card.CardWith4Color
      }, { TheCardIsAColorCardException(playerId) })
      .checkState({
        (card is Card.CardWith4Color && chosenColor != null) || card is Card.CardWithColor
      }, { TheCardHasNoColorException(playerId) })
      .run(::applyEvent)

  internal fun applyEvent(event: CardIsPlayedEvent): GameStarted =
    run {
      val nextDirectionAfterPlay =
        when (event.card) {
          is Card.ReverseCard -> direction.revert()
          else -> direction
        }

      val color =
        when (event.card) {
          is Card.CardWithColor -> event.card.color
          is Card.CardWith4Color -> event.chosenColor!!
        }

      copy(
        players = players.withDropCardOnPlayerHand(event.playerId, event.card),
        discardPile = discardPile.withNewCard(card = event.card),
        currentColor = color,
        lastPlayerId = event.playerId,
        nextPlayerId = players.nextPlayerTurn(event.playerId, nextDirectionAfterPlay),
        playedTurnHistory = playedTurnHistory - History(event.playerId, event, direction),
        direction = nextDirectionAfterPlay,
        version = event.version,
        recordedEvents = recordedEvents + event,
      )
    }

  fun playerTakeCartFromDrawPile(
    playerId: Player.PlayerId,
    number: Int,
  ): GameStarted {
    val takenCards = drawPile.take(number).second
    return PlayerHaveDrawCardEvent(aggregateId, playerId, takenCards, version + 1)
      .checkPlayerTurn()
      .checkState({
        playableCards(playerId).isEmpty()
      }, {
        ThePlayerMustPlayACardException(playerId, playableCards(playerId))
      })
      .run(::applyEvent)
      .run {
        val missingCardsCount = number - takenCards.size
        if (missingCardsCount > 0) {
          fillDrawWithDiscard()
            .playerTakeCartFromDrawPile(playerId, missingCardsCount)
        } else {
          this
        }
      }
  }

  internal fun applyEvent(event: PlayerHaveDrawCardEvent): GameStarted =
    copy(
      players = players.withNewCardOnPlayerHand(event.playerId, event.takenCards),
      drawPile = drawPile.take(event.takenCards.size).first,
      lastPlayerId = event.playerId,
      nextPlayerId = players.nextPlayerTurn(event.playerId, direction),
      version = event.version,
      recordedEvents = recordedEvents + event,
    )

  /**
   * Filling the draw pile with the discard pile while excluding the top card
   */
  private fun fillDrawWithDiscard(): GameStarted =
    run {
      val topCard = discardPile.topCard ?: throw InconsistentGameException(this)
      DrawPile(discardPile.cards - topCard).shuffled() to DiscardPile(setOf(topCard))
    }.let { (newDrawPile, newDiscardPile) ->
      DrawFilledWithDiscardEvent(aggregateId, newDrawPile, newDiscardPile, version + 1)
        .run(::applyEvent)
    }

  internal fun applyEvent(event: DrawFilledWithDiscardEvent): GameStarted =
    copy(
      drawPile = event.newDrawPile,
      discardPile = event.newDiscardPile,
      version = event.version,
      recordedEvents = recordedEvents + event,
    )

  fun playerWin(playerId: Player.PlayerId): GameStarted =
    PlayerWinEvent(aggregateId, playerId, version + 1)
      .checkState({
        players
          .get(playerId)
          .hand.cards
          .isEmpty()
      }, { ThePlayerHasRemainingCardsException(players.get(playerId)) })
      .checkState({ playerWins.contains(playerId) }, { ThePlayerHasAlreadyWinException(playerId) })
      .checkState({ !players.map { it.id }.contains(playerId) }, { ThePlayerIsNotInTheGameException(playerId) })
      .run(::applyEvent)

  internal fun applyEvent(event: PlayerWinEvent): GameStarted =
    copy(
      playerWins = playerWins + event.playerId,
      version = event.version,
      recordedEvents = recordedEvents + event,
    )

  private fun <T : PlayerActionEvent> T.checkPlayerTurn(): T =
    checkState(
      { nextPlayer.id == playerId },
      { ItsNotTheTurnException(playerId, this) },
    )

  private fun isPlayedLastTurn(card: Card): Boolean =
    (playedTurnHistory.last().event as? CardIsPlayedEvent)?.card == card
}
