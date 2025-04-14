package eventDemo.adapter.interfaceLayer.query

import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.CardIsPlayedEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.event.disableShuffleDeck
import eventDemo.business.event.projection.gameState.GameState
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.testApplicationWithConfig
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class GameStateRouteTest :
  FunSpec({
    test("""The route "/games/{id}/state" should returns a non started game when is not exist""") {
      testApplicationWithConfig {
        val gameId = GameId()
        val player1 = Player(name = "Nikola")

        httpClient()
          .get("/games/$gameId/state") {
            withAuth(player1)
            accept(ContentType.Application.Json)
          }.apply {
            assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
            call.body<GameState>().apply {
              aggregateId shouldBeEqual gameId
              players shouldHaveSize 0
              isStarted shouldBeEqual false
            }
          }
      }
    }

    test("""The route "/games/{id}/state" should returns the state with all informations""") {
      val gameId = GameId()
      val player1 = Player(name = "Nikola")
      val player2 = Player(name = "Einstein")
      var lastPlayedCard: Card? = null
      testApplicationWithConfig({
        disableShuffleDeck()
        val eventHandler = get<GameEventHandler>()
        val stateRepo = get<GameStateRepository>()

        runBlocking {
          eventHandler.handle(gameId) { NewPlayerEvent(gameId, player1, it) }
          eventHandler.handle(gameId) { NewPlayerEvent(gameId, player2, it) }
          eventHandler.handle(gameId) { PlayerReadyEvent(gameId, player1, it) }
          eventHandler.handle(gameId) { PlayerReadyEvent(gameId, player2, it) }
          lastPlayedCard = eventually { stateRepo.getLast(gameId).playableCards(player1).first() }
          assertNotNull(lastPlayedCard)
            .let { assertIs<Card.NumericCard>(lastPlayedCard) }
            .let {
              it.number shouldBeEqual 0
              it.color shouldBeEqual Card.Color.Red
            }
          eventHandler.handle(gameId) {
            CardIsPlayedEvent(
              gameId,
              assertNotNull(lastPlayedCard),
              player1,
              it,
            )
          }
        }
      }) {
        eventually(1.seconds) {
          httpClient()
            .get("/games/$gameId/state") {
              withAuth(player1)
              accept(ContentType.Application.Json)
            }.apply {
              assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
              call.body<GameState>().apply {
                aggregateId shouldBeEqual gameId
                players shouldHaveSize 2
                isStarted shouldBeEqual true
                assertIs<CardIsPlayedEvent>(lastEvent)
                readyPlayers shouldBeEqual setOf(player1, player2)
                direction shouldBeEqual GameState.Direction.CLOCKWISE
                assertNotNull(lastCardPlayer) shouldBeEqual player1
                assertNotNull(colorOnCurrentStack) shouldBeEqual Card.Color.Red
              }
            }
        }
      }
    }

    test("""The route "/games/{id}/card/last" should return the last card played of the game""") {
      val gameId = GameId()
      val player1 = Player(name = "Nikola")
      val player2 = Player(name = "Einstein")
      var lastPlayedCard: Card? = null
      testApplicationWithConfig({
        disableShuffleDeck()
        val eventHandler = get<GameEventHandler>()
        val stateRepo = get<GameStateRepository>()

        runBlocking {
          eventHandler.handle(gameId) { NewPlayerEvent(gameId, player1, it) }
          eventHandler.handle(gameId) { NewPlayerEvent(gameId, player2, it) }
          eventHandler.handle(gameId) { PlayerReadyEvent(gameId, player1, it) }
          eventHandler.handle(gameId) { PlayerReadyEvent(gameId, player2, it) }
          lastPlayedCard = eventually { stateRepo.getLast(gameId).playableCards(player1).first() }
          assertNotNull(lastPlayedCard)
            .let { assertIs<Card.NumericCard>(lastPlayedCard) }
            .let {
              it.number shouldBeEqual 0
              it.color shouldBeEqual Card.Color.Red
            }
          eventHandler.handle(gameId) {
            CardIsPlayedEvent(
              gameId,
              assertNotNull(lastPlayedCard),
              player1,
              it,
            )
          }
        }
      }) {
        eventually(1.seconds) {
          httpClient()
            .get("/games/$gameId/card/last") {
              withAuth(player1)
              accept(ContentType.Application.Json)
            }.apply {
              assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
              assertEquals(assertNotNull(lastPlayedCard), call.body<Card>())
            }
        }
      }
    }
  })
