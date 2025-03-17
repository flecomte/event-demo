package eventDemo.app.query

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.projection.gameList.GameList
import eventDemo.configuration.configure
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
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameListRouteTest :
  FunSpec({
    test("/games with no game started") {
      testApplication {
        val player1 = Player(name = "Nikola")
        application {
          stopKoin()
          configure()
        }

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
      testApplication {
        val gameId = GameId()
        val player1 = Player(name = "Nikola")

        application {
          stopKoin()
          configure()

          val eventHandler by inject<GameEventHandler>()
          runBlocking {
            eventHandler.handle(gameId) { NewPlayerEvent(gameId, player1, it) }
          }
        }

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

    test("/games return a game with status IS_STARTED") {
      testApplication {
        val gameId = GameId()
        val player1 = Player(name = "Nikola")
        val player2 = Player(name = "Einstein")

        application {
          stopKoin()
          configure()

          val eventHandler by inject<GameEventHandler>()
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
        }

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
  })
