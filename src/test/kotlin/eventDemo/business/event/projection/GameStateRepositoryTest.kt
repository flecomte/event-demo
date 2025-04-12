package eventDemo.business.event.projection

import ch.qos.logback.classic.Level
import com.rabbitmq.client.impl.ForgivingExceptionHandler
import com.zaxxer.hikari.pool.ProxyConnection
import eventDemo.Tag
import eventDemo.business.command.GameCommandHandler
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.projection.gameState.GameState
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.testKoinApplicationWithConfig
import eventDemo.withLogLevel
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.common.KotestInternal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.slf4j.Logger
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class, KotestInternal::class)
class GameStateRepositoryTest :
  FunSpec({
    tags(Tag.Postgresql)

    val player1 = Player("Tesla")
    val player2 = Player(name = "Einstein")

    test("GameStateRepository should build the projection when a new event occurs") {
      val aggregateId = GameId()
      testKoinApplicationWithConfig {
        val repo = get<GameStateRepository>()
        val eventHandler = get<GameEventHandler>()
        eventHandler
          .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player1, version = it) }
          .also { event ->
            // Wait until the projection is created
            eventually(1.seconds) {
              assertNotNull(repo.getUntil(event)).also {
                assertNotNull(it.players) shouldBeEqual setOf(player1)
              }
              assertNotNull(repo.getLast(aggregateId)).also {
                assertNotNull(it.players) shouldBeEqual setOf(player1)
              }
            }
          }
      }
    }

    test("get should build the last version of the state") {
      withLogLevel(
        GameCommandHandler::class.java.name to Level.ERROR,
        ForgivingExceptionHandler::class.java.name to Level.OFF,
      ) {
        val aggregateId = GameId()
        testKoinApplicationWithConfig {
          val repo = get<GameStateRepository>()
          val eventHandler = get<GameEventHandler>()
          val projectionBus = get<GameProjectionBus>()

          var state: GameState? = null
          projectionBus.subscribe {
            repo.getLast(aggregateId).also {
              state = it
            }
          }

          eventHandler
            .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player1, version = it) }
            .also {
              eventually(1.seconds) {
                assertNotNull(state).players.isNotEmpty() shouldBeEqual true
                assertNotNull(state).players shouldBeEqual setOf(player1)
              }
            }

          eventHandler
            .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player2, version = it) }
            .also {
              eventually(1.seconds) {
                assertNotNull(repo.getLast(aggregateId)).also {
                  assertNotNull(it.players) shouldBeEqual setOf(player1, player2)
                }
              }
            }
        }
      }
    }

    test("getUntil should build the state until the event") {
      withLogLevel(
        GameCommandHandler::class.java.name to Level.ERROR,
        ForgivingExceptionHandler::class.java.name to Level.OFF,
        ProxyConnection::class.java.name to Level.OFF,
        Logger.ROOT_LOGGER_NAME to Level.INFO,
      ) {
        repeat(10) {
          val aggregateId = GameId()
          testKoinApplicationWithConfig {
            val repo = get<GameStateRepository>()
            val eventHandler = get<GameEventHandler>()

            val event1 =
              eventHandler
                .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player1, version = it) }
                .also { event1 ->
                  assertNotNull(repo.getUntil(event1)).also {
                    assertNotNull(it.players) shouldBeEqual setOf(player1)
                  }
                }

            eventHandler
              .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player2, version = it) }
              .also { event2 ->
                assertNotNull(repo.getUntil(event2)).also {
                  assertNotNull(it.players) shouldBeEqual setOf(player1, player2)
                }
                assertNotNull(repo.getUntil(event1)).also {
                  assertNotNull(it.players) shouldBeEqual setOf(player1)
                }
              }
          }
        }
      }
    }

    test("getUntil should be concurrently secure").config(tags = setOf(Tag.Concurrence)) {
      withLogLevel(
        Logger.ROOT_LOGGER_NAME to Level.ERROR,
        ForgivingExceptionHandler::class.java.name to Level.OFF,
      ) {
        val aggregateId = GameId()
        testKoinApplicationWithConfig {
          val repo = get<GameStateRepository>()
          val eventHandler = get<GameEventHandler>()

          (1..10)
            .map { r ->
              GlobalScope
                .launch {
                  repeat(20) { r2 ->
                    val playerX = Player("player$r$r2")
                    eventHandler
                      .handle(aggregateId) {
                        NewPlayerEvent(
                          aggregateId = aggregateId,
                          player = playerX,
                          version = it,
                        )
                      }
                  }
                }
            }.joinAll()

          eventually(
            eventuallyConfig {
              duration = 10.seconds
              interval = 1.seconds
              includeFirst = false
            },
          ) {
            repo.getLast(aggregateId).run {
              lastEventVersion shouldBeEqual 200
              players shouldHaveSize 200
            }
            repo.count(aggregateId) shouldBe 21
          }
        }
      }
    }

    xtest("get should be concurrently secure") {
      tags(Tag.Concurrence)
    }
  })
