package eventDemo.contexts.game.intrastructure.rest

import eventDemo.contexts.auth.application.eventStores.UserRepository
import eventDemo.contexts.game.intrastructure.httpClient
import eventDemo.contexts.game.intrastructure.withAuth
import eventDemo.shared.game.projection.GameList
import eventDemo.testHelpers.CreateGameWithCommandsHelpers
import eventDemo.testHelpers.CreateGameWithCommandsHelpers.joinTheGame
import eventDemo.testHelpers.CreateGameWithCommandsHelpers.readyToPlay
import eventDemo.testHelpers.createNewUser
import eventDemo.testHelpers.testApplicationWithConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.eventuallyConfig
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

val logger = KotlinLogging.logger {}

class GameListRouteTest :
  FunSpec({
    test("/games with no game started") {
      val user1 = createNewUser("user1")
      testApplicationWithConfig({
        get<UserRepository>().save(user1)
      }) {
        logger.info { "Starting player1" }
        httpClient()
          .get("/games") {
            withAuth(user1)
            accept(ContentType.Application.Json)
          }.apply {
            assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
            val list = call.body<List<GameList>>()
            assertTrue(list.isEmpty())
          }
      }
    }

    test("/games return a game with status OPENING") {
      val user1 = createNewUser("user1")
      testApplicationWithConfig({
        get<UserRepository>().save(user1)
        CreateGameWithCommandsHelpers.createGameWithCommands {
          user1.joinTheGame()
        }
      }) {
        // Wait until the projection is created
        eventually(
          eventuallyConfig {
            initialDelay = 1.seconds
            interval = 1.seconds
            duration = 3.seconds
          },
        ) {
          httpClient()
            .get("/games") {
              withAuth(user1)
              accept(ContentType.Application.Json)
            }.apply {
              assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
              call.body<List<GameList>>().let {
                assertNotNull(it)
                assertTrue { it.isNotEmpty() }
                it.firstOrNull()?.run {
                  status shouldBeEqual GameList.Status.OPENING
                  players shouldHaveSize 1
                  players.map { it.userId } shouldContain user1.id
                  winners shouldHaveSize 0
                }
              }
            }
        }
      }
    }

    test("/games return a game with status IS_STARTED") {
      val user1 = createNewUser("user1")
      val user2 = createNewUser("user2")
      testApplicationWithConfig({
        CreateGameWithCommandsHelpers.createGameWithCommands {
          get<UserRepository>().run {
            save(user1)
            save(user2)
          }
          user1.joinTheGame()
          user2.joinTheGame()
          getPlayer(user1).readyToPlay()
          getPlayer(user2).readyToPlay()
        }
      }) {
        eventually(
          eventuallyConfig {
            initialDelay = 1.seconds
            interval = 1.seconds
            duration = 3.seconds
          },
        ) {
          httpClient()
            .get("/games") {
              withAuth(user1)
              accept(ContentType.Application.Json)
            }.apply {
              assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
              call.body<List<GameList>>().first().let {
                it.status shouldBeEqual GameList.Status.IS_STARTED
                it.players shouldHaveSize 2
                it.players.map { it.userId } shouldContain user1.id
                it.players.map { it.userId } shouldContain user2.id
                it.winners shouldHaveSize 0
              }
            }
        }
      }
    }
  })
