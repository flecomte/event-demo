package eventDemo.app.event.projection

import eventDemo.business.entity.Card
import eventDemo.business.entity.Deck
import eventDemo.business.entity.Discard
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.entity.PlayersHands
import eventDemo.business.event.projection.gameState.GameState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual

class GameStateTest :
  FunSpec({
    val player1 = Player("Tesla")
    val player2 = Player("Einstein")

    test("isReady return false when all players in the game is not ready") {
      GameState(
        aggregateId = GameId(),
        players = setOf(player1, player2),
        readyPlayers = setOf(player1),
      ).isReady shouldBeEqual false
    }

    test("isReady return true when all players in the game is ready") {
      GameState(
        aggregateId = GameId(),
        players = setOf(player1, player2),
        readyPlayers = setOf(player1, player2),
      ).isReady shouldBeEqual true
    }

    xtest("nextPlayerTurn") { }
    xtest("playerDiffIndex") { }
    xtest("cardOnBoardIsForYou") { }
    xtest("playableCards") { }
    xtest("playerHasNoCardLeft") { }

    test("canBePlayThisCard return true when a card can be played on the current game") {
      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.NumericCard(5, Card.Color.Red),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.NumericCard(0, Card.Color.Blue),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.NumericCard(0, Card.Color.Green),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.Plus2Card(Card.Color.Red),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.Plus2Card(Card.Color.Red),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.Plus4Card(),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.Plus4Card(),
        playedCard = Card.Plus4Card(),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.Plus4Card(),
        playedCard = Card.Plus4Card(),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.Plus2Card(Card.Color.Red),
        playedCard = Card.Plus2Card(Card.Color.Blue),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.ChangeColorCard(),
      ) shouldBeEqual true

      canBePlayThisCard(
        onTheDeck = Card.ReverseCard(Card.Color.Red),
        playedCard = Card.NumericCard(0, Card.Color.Red),
      ) shouldBeEqual true
    }

    test("canBePlayThisCard return false when a card cannot be played on the current game") {
      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.NumericCard(9, Card.Color.Blue),
      ) shouldBeEqual false

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.Plus2Card(Card.Color.Blue),
      ) shouldBeEqual false

      canBePlayThisCard(
        onTheDeck = Card.NumericCard(0, Card.Color.Red),
        playedCard = Card.Plus2Card(Card.Color.Blue),
      ) shouldBeEqual false

      canBePlayThisCard(
        onTheDeck = Card.Plus2Card(Card.Color.Red),
        playedCard = Card.Plus4Card(),
      ) shouldBeEqual false

      canBePlayThisCard(
        onTheDeck = Card.Plus2Card(Card.Color.Red),
        playedCard = Card.ChangeColorCard(),
      ) shouldBeEqual false

      canBePlayThisCard(
        onTheDeck = Card.Plus2Card(Card.Color.Red),
        playedCard = Card.NumericCard(0, Card.Color.Red),
      ) shouldBeEqual false
    }
  })

private fun canBePlayThisCard(
  onTheDeck: Card,
  playedCard: Card,
): Boolean {
  val player1 = Player("Tesla")
  return gameStateWithCard(
    player = player1,
    onTheDeck = onTheDeck,
    playerHand = listOf(playedCard),
  ).canBePlayThisCard(player1, playedCard)
}

private fun gameStateWithCard(
  player: Player,
  onTheDeck: Card,
  playerHand: List<Card>,
): GameState {
  val player2 = Player("Einstein")
  return GameState(
    aggregateId = GameId(),
    players = setOf(player, player2),
    readyPlayers = setOf(player, player2),
    lastCardPlayer = player2,
    deck =
      Deck(
        discard = Discard(setOf(onTheDeck)),
        playersHands = PlayersHands(mapOf(player.id to playerHand)),
      ),
  )
}
