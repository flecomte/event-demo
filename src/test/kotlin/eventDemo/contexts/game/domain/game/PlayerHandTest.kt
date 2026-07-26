package eventDemo.contexts.game.domain.game

import eventDemo.testHelpers.act
import eventDemo.testHelpers.arrange
import eventDemo.testHelpers.assert
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeExactly
import org.junit.jupiter.api.assertInstanceOf

class PlayerHandTest :
  FunSpec({
    test("can be add new card to the hand") {
      arrange {
        PlayerHand(
          setOf(
            Card.NumericCard(0, Card.Color.Red),
            Card.NumericCard(1, Card.Color.Red),
            Card.NumericCard(2, Card.Color.Red),
          ),
        )
      }.act { hand ->
        hand.withNewCards(setOf(Card.NumericCard(3, Card.Color.Red)))
      }.assert { hand ->
        hand.size shouldBeExactly 4
      }
    }

    test("can be remove card to the hand") {
      arrange {
        PlayerHand(
          setOf(
            Card.NumericCard(0, Card.Color.Red),
            Card.NumericCard(1, Card.Color.Red),
            Card.NumericCard(2, Card.Color.Red),
          ),
        )
      }.act { hand ->
        hand.withoutTheCards(setOf(hand.cards.elementAt(0)))
      }.assert { hand ->
        hand.size shouldBeExactly 2
        assertInstanceOf<Set<Card.NumericCard>>(hand.cards)
        hand.cards.elementAt(0).number shouldBeExactly 1
        hand.cards.elementAt(1).number shouldBeExactly 2
      }
    }
  })
