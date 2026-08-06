package eventDemo.contexts.game.application.eventStore

import ch.qos.logback.classic.Level
import com.rabbitmq.client.impl.ForgivingExceptionHandler
import eventDemo.Tag
import eventDemo.contexts.auth.application.eventStores.UserRepository
import eventDemo.contexts.game.application.eventStores.GameEventStoreRepository
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.shared.ids.GameId
import eventDemo.testHelpers.CreateGameWithCommandsHelpers
import eventDemo.testHelpers.CreateGameWithCommandsHelpers.joinTheGame
import eventDemo.testHelpers.createNewUser
import eventDemo.testHelpers.testKoinApplicationWithConfig
import eventDemo.testHelpers.withLogLevel
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.slf4j.Logger
import java.util.UUID
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class GameEventStoreRepositoryTest :
  FunSpec({
    tags(Tag.Postgresql)

    val user1 = createNewUser("user1")
    val user2 = createNewUser("user2")

    test("GameRepository should build return the game after dispatch commands") {
      testKoinApplicationWithConfig {
        get<UserRepository>().run {
          save(user1)
        }
        CreateGameWithCommandsHelpers.createGameWithCommands {
          user1.joinTheGame()
          getPlayer(user1).userId shouldBeEqual user1.id
        }
      }
    }

    test("get should build the last version of the state") {
      withLogLevel(
        ForgivingExceptionHandler::class.java.name to Level.OFF,
      ) {
        testKoinApplicationWithConfig {
          val repo = get<GameEventStoreRepository>()
          get<UserRepository>().run {
            save(user1)
            save(user2)
          }

          CreateGameWithCommandsHelpers.createGameWithCommands {
            user1.joinTheGame()
            assertNotNull(repo.get(gameId)).run {
              players.isNotEmpty() shouldBeEqual true
              players.get(user1.id).userId shouldBeEqual user1.id
            }
            user2.joinTheGame()
            assertNotNull(repo.get(gameId)).run {
              players.isNotEmpty() shouldBeEqual true
              players.size shouldBeEqual 2
              players.get(user1.id).userId shouldBeEqual user1.id
              players.get(user2.id).userId shouldBeEqual user2.id
            }
          }
        }
      }
    }

    test("get should be concurrently secure").config(tags = setOf(Tag.Concurrence)) {
      withLogLevel(
        Logger.ROOT_LOGGER_NAME to Level.ERROR,
        ForgivingExceptionHandler::class.java.name to Level.OFF,
      ) {
        var aggregateIds: MutableList<GameId> = mutableListOf()
        testKoinApplicationWithConfig {
          val repo = get<GameRepository>()

          val gameName = "testGame${UUID.randomUUID()}"
          (1..2)
            .map { treadN ->
              GlobalScope
                .launch {
                  CreateGameWithCommandsHelpers.createGameWithCommands("testGame $treadN") {
                    repeat(3) { userN ->
                      val userX = createNewUser("userX $treadN:$userN")
                      get<UserRepository>().save(userX)
                      userX.joinTheGame()
                    }
                    aggregateIds.add(gameId)
                  }
                }
            }.joinAll()

          eventually(
            eventuallyConfig {
              duration = 5.seconds
              interval = 1.seconds
              includeFirst = false
            },
          ) {
            aggregateIds shouldHaveSize 2
            aggregateIds.forEach {
              assertNotNull(repo.get(it)).run {
                version shouldBeEqual 4
                players shouldHaveSize 3
              }
            }
          }
        }
      }
    }
  })
