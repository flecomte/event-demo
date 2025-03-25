package eventDemo.business.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.configuration.injection.Configuration
import eventDemo.configuration.injection.appKoinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import kotlin.test.assertNotNull

@OptIn(DelicateCoroutinesApi::class)
class GameStateRepositoryTest :
  FunSpec({
    val player1 = Player("Tesla")
    val player2 = Player(name = "Einstein")

    test("GameStateRepository should build the projection when a new event occurs") {
      val aggregateId = GameId()
      koinApplication { modules(appKoinModule(Configuration("redis://localhost:6379"))) }.koin.apply {
        val repo = get<GameStateRepository>()
        val eventHandler = get<GameEventHandler>()
        eventHandler
          .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player1, version = it) }
          .also { event ->
            assertNotNull(repo.getUntil(event)).also {
              assertNotNull(it.players) shouldBeEqual setOf(player1)
            }
            assertNotNull(repo.getLast(aggregateId)).also {
              assertNotNull(it.players) shouldBeEqual setOf(player1)
            }
          }
      }
      stopKoin()
    }

    test("get should build the last version of the state") {
      val aggregateId = GameId()
      koinApplication { modules(appKoinModule(Configuration("redis://localhost:6379"))) }.koin.apply {
        val repo = get<GameStateRepository>()
        val eventHandler = get<GameEventHandler>()

        eventHandler
          .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player1, version = it) }
          .also {
            assertNotNull(repo.getLast(aggregateId)).also {
              assertNotNull(it.players) shouldBeEqual setOf(player1)
            }
          }

        eventHandler
          .handle(aggregateId) { NewPlayerEvent(aggregateId = aggregateId, player = player2, version = it) }
          .also {
            assertNotNull(repo.getLast(aggregateId)).also {
              assertNotNull(it.players) shouldBeEqual setOf(player1, player2)
            }
          }
      }
    }

    test("getUntil should build the state until the event") {
      repeat(10) {
        val aggregateId = GameId()
        koinApplication { modules(appKoinModule(Configuration("redis://localhost:6379"))) }.koin.apply {
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

    test("getUntil should be concurrently secure") {
      val aggregateId = GameId()
      koinApplication { modules(appKoinModule(Configuration("redis://localhost:6379"))) }.koin.apply {
        val repo = get<GameStateRepository>()
        val eventHandler = get<GameEventHandler>()

        (1..10)
          .map { r ->
            GlobalScope
              .launch {
                repeat(100) { r2 ->
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

        repo.getLast(aggregateId).run {
          lastEventVersion shouldBeEqual 1000
          players shouldHaveSize 1000
        }
      }
    }

    xtest("get should be concurrently secure") { }
  })
