package eventDemo.contexts.game.domain.game.gameState

import eventDemo.shared.game.Card
import io.kotest.assertions.retry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.time.Duration.Companion.seconds

class NewDeckTest :
  FunSpec({

    test("newDeck") {
      newDeck().let {
        it shouldNotBe null
        it.filterIsInstance<Card.NumericCard>() shouldHaveSize 76
        it.filterIsInstance<Card.Plus2Card>() shouldHaveSize 8
        it.filterIsInstance<Card.ReverseCard>() shouldHaveSize 8
        it.filterIsInstance<Card.PassCard>() shouldHaveSize 8
        it.filterIsInstance<Card.Plus4Card>() shouldHaveSize 4
        it.filterIsInstance<Card.ChangeColorCard>() shouldHaveSize 4
        it shouldHaveSize 108
      }
    }

    test("shuffleDeck") {
      val deck = (0..9).map { Card.NumericCard(it, Card.Color.Red) }
      deck.run {
        this[3].number shouldBe 3
      }
      should {
        retry(maxRetry = 4, timeout = 1.seconds) {
          deck.shuffled().run {
            this[3].number shouldNotBe 3
          }
        }
      }
    }
  })
