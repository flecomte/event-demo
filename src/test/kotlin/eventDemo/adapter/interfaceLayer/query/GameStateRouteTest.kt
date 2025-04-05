package eventDemo.adapter.interfaceLayer.query

import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.CardIsPlayedEvent
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.projection.gameState.GameState
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.testApplicationWithConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GameStateRouteTest :
  FunSpec({
    test("/games/{id}/state on empty game") {
      testApplicationWithConfig {
        val id = GameId()
        val player1 = Player(name = "Nikola")

        httpClient()
          .get("/games/$id/state") {
            withAuth(player1)
            accept(ContentType.Application.Json)
          }.apply {
            assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
            val state = call.body<GameState>()
            id shouldBeEqual state.aggregateId
            state.players shouldHaveSize 0
            state.isStarted shouldBeEqual false
          }
      }
    }

    test("/games/{id}/card/last") {
      val gameId = GameId()
      val player1 = Player(name = "Nikola")
      val player2 = Player(name = "Einstein")
      var lastPlayedCard: Card? = null
      testApplicationWithConfig({
        val eventHandler = get<GameEventHandler>()
        val stateRepo = get<GameStateRepository>()

        runBlocking {
          eventHandler.handle(gameId) { NewPlayerEvent(gameId, player1, it) }
          eventHandler.handle(gameId) { NewPlayerEvent(gameId, player2, it) }
          eventHandler.handle(gameId) { PlayerReadyEvent(gameId, player1, it) }
          eventHandler.handle(gameId) { PlayerReadyEvent(gameId, player2, it) }
          eventHandler.handle(gameId) {
            GameStartedEvent.new(
              gameId,
              setOf(player1, player2),
              it,
              shuffleIsDisabled = true,
            )
          }
          delay(100)
          lastPlayedCard = stateRepo.getLast(gameId).playableCards(player1).first()
          assertNotNull(lastPlayedCard)
            .let { assertIs<Card.NumericCard>(lastPlayedCard) }
            .let {
              it.number shouldBeEqual 0
              it.color shouldBeEqual Card.Color.Red
            }
          delay(100)
          eventHandler.handle(gameId) {
            CardIsPlayedEvent(
              gameId,
              assertNotNull(lastPlayedCard),
              player1,
              it,
            )
          }
          delay(100)
        }
      }) {
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
  })
