package eventDemo.contexts.game.domain.game.gameState

import eventDemo.contexts.game.domain.events.CardIsPlayedEvent
import eventDemo.contexts.game.domain.events.DrawFilledWithDiscardEvent
import eventDemo.contexts.game.domain.events.PlayerHaveDrawCardEvent
import eventDemo.contexts.game.domain.events.PlayerWinEvent
import eventDemo.contexts.game.domain.game.Card
import eventDemo.contexts.game.domain.game.DiscardPile
import eventDemo.contexts.game.domain.game.DrawPile
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.domain.game.PlayerHand
import eventDemo.contexts.game.domain.game.PlayerList
import eventDemo.contexts.game.domain.game.gameState.Game.Direction
import eventDemo.sharedKernel.UserId
import eventDemo.testHelpers.act
import eventDemo.testHelpers.assert
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertInstanceOf

class GameStartedTest :
  FunSpec({
    context(GameStarted::canBePlayThisCard.name) {
      val dataOk: Map<String, Triple<Card, Card, Card.Color?>> =
        listOf(
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.NumericCard(5, Card.Color.Red),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.NumericCard(0, Card.Color.Blue),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Green),
            Card.NumericCard(0, Card.Color.Red),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.Plus2Card(Card.Color.Red),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.Plus2Card(Card.Color.Red),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.Plus4Card(),
            null,
          ),
          Triple(
            Card.Plus4Card(),
            Card.Plus4Card(),
            Card.Color.Red,
          ),
          Triple(
            Card.Plus4Card(),
            Card.NumericCard(0, Card.Color.Blue),
            Card.Color.Blue,
          ),
          Triple(
            Card.Plus2Card(Card.Color.Red),
            Card.Plus2Card(Card.Color.Blue),
            null,
          ),
          Triple(
            Card.Plus2Card(Card.Color.Red),
            Card.Plus4Card(),
            null,
          ),
          Triple(
            Card.Plus2Card(Card.Color.Red),
            Card.ChangeColorCard(),
            null,
          ),
          Triple(
            Card.Plus2Card(Card.Color.Red),
            Card.NumericCard(0, Card.Color.Red),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.ChangeColorCard(),
            null,
          ),
          Triple(
            Card.ReverseCard(Card.Color.Red),
            Card.NumericCard(0, Card.Color.Red),
            null,
          ),
        ).associateBy { "I can play ${it.second} on ${it.first}${if (it.third != null) " when choose color is ${it.third}" else ""}" }

      withData(dataOk) {
        canBePlayThisCard(
          it.first,
          it.second,
          it.third,
        ) shouldBe true
      }

      val dataKo: Map<String, Triple<Card, Card, Card.Color?>> =
        listOf(
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.NumericCard(9, Card.Color.Blue),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.Plus2Card(Card.Color.Blue),
            null,
          ),
          Triple(
            Card.NumericCard(0, Card.Color.Red),
            Card.Plus2Card(Card.Color.Blue),
            null,
          ),
          Triple(
            Card.Plus4Card(),
            Card.NumericCard(0, Card.Color.Blue),
            Card.Color.Red,
          ),
        ).associateBy { "I cannot play ${it.second} on ${it.first}${if (it.third != null) " when choose color is ${it.third}" else ""}" }

      withData(dataKo) {
        canBePlayThisCard(
          it.first,
          it.second,
          it.third,
        ) shouldBe false
      }
    }

    context("applyEvent") {
      context("${CardIsPlayedEvent::class.simpleName}") {
        test("with numeric card") {
          val card1 = Card.NumericCard(2, Card.Color.Red)
          assert {
            gameWithCard(
              played1Hand = PlayerHand(cards = setOf(card1)),
              onTheDiscardPile = Card.NumericCard(0, Card.Color.Red),
            ).apply { nextPlayer shouldBe player1 }
          }.act {
            it.applyEvent(
              CardIsPlayedEvent(
                it.aggregateId,
                card1,
                playerId = it.player1.id,
                version = it.version + 1,
              ),
            )
          }.assert {
            it.currentColor shouldBe Card.Color.Red
            assertInstanceOf<Card.NumericCard>(it.lastPlayedCard).number shouldBe 2
            it.direction shouldBe Game.Direction.CLOCKWISE
            it.nextPlayer shouldBe it.player2
            it.player1.hand.size shouldBe 0
          }
        }
        test("with revert turn card") {
          val card1 = Card.ReverseCard(Card.Color.Red)
          assert {
            gameWithCard(
              played1Hand = PlayerHand(cards = setOf(card1)),
              onTheDiscardPile = Card.NumericCard(0, Card.Color.Red),
            ).apply { nextPlayer shouldBe player1 }
          }.act {
            it.applyEvent(
              CardIsPlayedEvent(
                it.aggregateId,
                card1,
                playerId = it.player1.id,
                version = it.version + 1,
              ),
            )
          }.assert {
            it.currentColor shouldBe Card.Color.Red
            assertInstanceOf<Card.ReverseCard>(it.lastPlayedCard).color shouldBe Card.Color.Red
            it.direction shouldBe Game.Direction.COUNTER_CLOCKWISE
            it.nextPlayer shouldBe it.player3
            it.player1.hand.size shouldBe 0
          }
        }
      }

      test("${DrawFilledWithDiscardEvent::class.simpleName}") {
        val card1 = Card.NumericCard(1, Card.Color.Blue)
        val card2 = Card.NumericCard(2, Card.Color.Yellow)
        val card3 = Card.NumericCard(3, Card.Color.Red)
        assert {
          val player1 = Player("Player 1", UserId())
          val player2 = Player("Player 2", UserId())
          GameStarted(
            aggregateId = GameId(),
            players = PlayerList(setOf(player1)),
            lastPlayerId = player1.id,
            nextPlayerId = player2.id,
            drawPile = DrawPile(),
            discardPile =
              DiscardPile(
                setOf(
                  card1,
                  card2,
                  card3,
                ),
              ),
            currentColor = card3.color,
            version = 0,
            recordedEvents = emptySet(),
          )
        }.act {
          it.applyEvent(
            DrawFilledWithDiscardEvent(
              it.aggregateId,
              version = it.version + 1,
              newDrawPile =
                DrawPile(
                  setOf(
                    card1,
                    card2,
                  ),
                ),
              newDiscardPile =
                DiscardPile(
                  setOf(
                    card3,
                  ),
                ),
            ),
          )
        }.assert {
          it.currentColor shouldBe Card.Color.Red
          assertInstanceOf<Card.NumericCard>(it.lastPlayedCard).color shouldBe Card.Color.Red
        }
      }

      test("${PlayerHaveDrawCardEvent::class.simpleName}") {
        val card1 = Card.NumericCard(1, Card.Color.Blue)
        val card2 = Card.NumericCard(2, Card.Color.Red)
        val card3 = Card.NumericCard(3, Card.Color.Red)
        val card4 = Card.NumericCard(4, Card.Color.Red)

        val player1 = Player("Jo", UserId())
        val player2 = Player("Bob", UserId())
        assert {
          GameStarted(
            aggregateId = GameId(),
            players = PlayerList(setOf(player1)),
            lastPlayerId = player1.id,
            nextPlayerId = player2.id,
            drawPile =
              DrawPile(
                setOf(
                  card2,
                  card3,
                  card4,
                ),
              ),
            discardPile =
              DiscardPile(
                setOf(
                  card1,
                ),
              ),
            currentColor = card1.color,
            version = 0,
            recordedEvents = emptySet(),
          )
        }.act {
          it.applyEvent(
            PlayerHaveDrawCardEvent(
              it.aggregateId,
              version = it.version + 1,
              playerId = it.player1.id,
              takenCards =
                setOf(
                  card2,
                  card3,
                ),
            ),
          )
        }.assert {
          it.player1.hand
            .run {
              cards.elementAt(0) shouldBe card2
              cards.elementAt(1) shouldBe card3
            }
        }
      }

      test("${PlayerWinEvent::class.simpleName}") {
        val player1 = Player("Jo", UserId())
        val player2 = Player("Poo", UserId())
        assert {
          GameStarted(
            aggregateId = GameId(),
            players = PlayerList(setOf(player1, player2)),
            lastPlayerId = player1.id,
            nextPlayerId = player2.id,
            drawPile = DrawPile(),
            discardPile = DiscardPile(),
            currentColor = Card.Color.Yellow,
            version = 0,
            recordedEvents = emptySet(),
          )
        }.act {
          it.applyEvent(
            PlayerWinEvent(
              it.aggregateId,
              version = it.version + 1,
              playerId = it.player1.id,
            ),
          )
        }.assert {
          it.playerWins.size shouldBe 1
          it.playersInGame.size shouldBe 0
          it.players.size shouldBe 2
        }
      }
    }

    test("nextPlayer") { }

    test("players") { }

    test("currentColor") { }

    test("playedTurnHistory") { }

    test("direction") { }

    test("playerWins") { }

    test("version") { }

    test("recordedEvents") { }
  })

private val GameStarted.player1: Player
  get() = players.players.elementAt(0)

private val GameStarted.player2: Player
  get() = players.players.elementAt(1)

private val GameStarted.player3: Player
  get() = players.players.elementAt(2)

private fun canBePlayThisCard(
  onTheDiscardPile: Card,
  playedCard: Card,
  chosenColor: Card.Color? = null,
): Boolean =
  gameWithCard(
    played1Hand = PlayerHand(setOf(playedCard)),
    onTheDiscardPile = onTheDiscardPile,
    chosenColor = chosenColor,
  ).run {
    canBePlayThisCard(playedCard)
  }

private fun gameWithCard(
  played1Hand: PlayerHand,
  played2Hand: PlayerHand = PlayerHand(setOf(Card.NumericCard(9, Card.Color.Yellow))),
  onTheDiscardPile: Card,
  chosenColor: Card.Color? = null,
): GameStarted {
  val player1 = Player("Tesla", UserId(), hand = played1Hand)
  val player2 = Player("Einstein", UserId(), hand = played2Hand)
  val player3 = Player("Curie", UserId(), hand = PlayerHand(setOf(Card.NumericCard(8, Card.Color.Yellow))))
  val players = PlayerList(setOf(player1, player2, player3))
  return GameStarted(
    aggregateId = GameId(),
    players = players,
    lastPlayerId = player3.id,
    nextPlayerId = players.nextPlayerTurn(player3.id, Direction.CLOCKWISE),
    discardPile = DiscardPile(setOf(onTheDiscardPile)),
    drawPile = DrawPile(),
    currentColor = (onTheDiscardPile as? Card.CardWithColor)?.color ?: chosenColor ?: error("no color"),
    version = 0,
    recordedEvents = emptySet(),
  )
}
