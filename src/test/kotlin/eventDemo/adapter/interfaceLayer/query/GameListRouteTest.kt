package eventDemo.adapter.interfaceLayer.query

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.projection.GameList
import eventDemo.testApplicationWithConfig
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
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
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class GameListRouteTest :
  FunSpec({
    test("/games with no game started") {

      testApplicationWithConfig {
        val player1 = Player(name = "Nikola")

        httpClient()
          .get("/games") {
            withAuth(player1)
            accept(ContentType.Application.Json)
          }.apply {
            assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
            val list = call.body<List<GameList>>()
            assertTrue(list.isEmpty())
          }
      }
    }

    test("/games return a game with status OPENING") {
      val gameId = GameId()
      val player1 = Player(name = "Nikola")
      testApplicationWithConfig(
        {
          runBlocking {
            get<GameEventHandler>()
              .handle(gameId) {
                NewPlayerEvent(gameId, player1, it)
              }
          }
        },
      ) {
        // Wait until the projection is created
        eventually(1.seconds) {
          httpClient()
            .get("/games") {
              withAuth(player1)
              accept(ContentType.Application.Json)
            }.apply {
              assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
              call.body<List<GameList>>().first().let {
                it.status shouldBeEqual GameList.Status.OPENING
                it.players shouldHaveSize 1
                it.players shouldContain player1
                it.winners shouldHaveSize 0
              }
            }
        }
      }
    }

    test("/games return a game with status IS_STARTED") {
      val gameId = GameId()
      val player1 = Player(name = "Nikola")
      val player2 = Player(name = "Einstein")
      testApplicationWithConfig({
        val eventHandler = get<GameEventHandler>()
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
        }
      }) {
        eventually(1.seconds) {
          httpClient()
            .get("/games") {
              withAuth(player1)
              accept(ContentType.Application.Json)
            }.apply {
              assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
              call.body<List<GameList>>().first().let {
                it.status shouldBeEqual GameList.Status.IS_STARTED
                it.players shouldHaveSize 2
                it.players shouldContain player1
                it.players shouldContain player2
                it.winners shouldHaveSize 0
              }
            }
        }
      }
    }
  })
