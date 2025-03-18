package eventDemo.business.entity

import eventDemo.business.entity.Card
import eventDemo.business.entity.Player
import eventDemo.business.entity.PlayersHands
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import kotlin.test.assertNotNull

class PlayerHandKtTest :
  FunSpec({
    test("addCards") {
      // Given
      val playerNumbers = 4
      val players = (1..playerNumbers).map { Player(name = "name $it") }.toSet()
      val firstPlayer = players.first()
      val playersHands = PlayersHands(players)
      val card = Card.NumericCard(0, Card.Color.Red)

      // When
      val newHands: PlayersHands = playersHands.addCards(firstPlayer, listOf(card))

      assertNotNull(newHands.getHand(firstPlayer)).size shouldBeExactly 1
      assertNotNull(newHands.getHand(players.last())).size shouldBeExactly 0
    }

    test("removeCard") {
      // Given
      val playerNumbers = 4
      val players = (1..playerNumbers).map { Player(name = "name $it") }.toSet()
      val firstPlayer = players.first()
      val card1 = Card.NumericCard(1, Card.Color.Red)
      val card2 = Card.NumericCard(2, Card.Color.Red)
      val playersHands: PlayersHands =
        PlayersHands(players)
          .addCards(firstPlayer, listOf(card1, card2))

      // When
      val newHands: PlayersHands = playersHands.removeCard(firstPlayer, card1)

      assertNotNull(newHands.getHand(firstPlayer)).size shouldBeExactly 1
      assertNotNull(newHands.getHand(players.last())).size shouldBeExactly 0
    }
  })
