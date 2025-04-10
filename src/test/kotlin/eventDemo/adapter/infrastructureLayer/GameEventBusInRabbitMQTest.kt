package eventDemo.adapter.infrastructureLayer

import com.rabbitmq.client.ConnectionFactory
import eventDemo.adapter.infrastructureLayer.event.GameEventBusInMemory
import eventDemo.adapter.infrastructureLayer.event.GameEventBusInRabbinMQ
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.testKoinApplicationWithConfig
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
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
          val spy = spyk(mockk<() -> Unit>())
          val aggregateId = GameId()
          val player1 = Player(name = "Tesla")
          val player2 = Player(name = "Einstein")

          bus.subscribe { obj ->
            spy()
            obj.aggregateId shouldBeEqual aggregateId
          }
          bus.publish(NewPlayerEvent(aggregateId, player1, 1))
          bus.publish(NewPlayerEvent(aggregateId, player2, 2))

          eventually(1.seconds) {
            verify(exactly = 2) { spy() }
          }
        }
      }
    }
  })
