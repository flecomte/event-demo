package eventDemo.contexts.game.intrastructure.persistence.eventBus

import com.rabbitmq.client.ConnectionFactory
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.events.NewPlayerEvent
import eventDemo.contexts.game.infrastructure.persistence.eventBus.GameEventBusInMemory
import eventDemo.contexts.game.infrastructure.persistence.eventBus.GameEventBusInRabbinMQ
import eventDemo.shared.game.Player
import eventDemo.shared.ids.GameId
import eventDemo.shared.ids.UserId
import eventDemo.testHelpers.spyPing
import eventDemo.testHelpers.testKoinApplicationWithConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equals.shouldBeEqual
import kotlin.time.Duration.Companion.seconds

class GameEventBusInRabbitMQTest :
  FunSpec({
    context("Pub/sub") {
      testKoinApplicationWithConfig {
        val busListToTest: Map<String, GameEventBus> =
          mapOf(
            GameEventBusInMemory::class.java.simpleName to GameEventBusInMemory(),
            GameEventBusInRabbinMQ::class.java.simpleName to GameEventBusInRabbinMQ(get<ConnectionFactory>()),
          )

        withData(busListToTest) { bus ->
          spyPing(1.seconds, exactly = 2) { ping ->
            val aggregateId = GameId()
            val player1 = Player(name = "Tesla", UserId())
            val player2 = Player(name = "Einstein", UserId())

            bus.subscribe { obj ->
              ping()
              obj.aggregateId shouldBeEqual aggregateId
            }
            bus.publish(NewPlayerEvent(aggregateId, player1, 1))
            bus.publish(NewPlayerEvent(aggregateId, player2, 2))
          }
        }
      }
    }
  })
