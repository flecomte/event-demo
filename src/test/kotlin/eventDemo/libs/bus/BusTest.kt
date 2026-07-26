package eventDemo.libs.bus

import com.rabbitmq.client.ConnectionFactory
import eventDemo.testHelpers.spyPing
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.string.shouldStartWith
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private data class ObjTest(
  val value: String,
)

class BusTest :
  FunSpec({
    context("Pub/sub") {
      val factory =
        ConnectionFactory().apply {
          host = "localhost"
          port = 5672
          username = "event-demo"
          password = "changeit"
        }
      val list: Map<String, Bus<ObjTest>> =
        mapOf(
          BusInMemory::class.java.simpleName to BusInMemory(),
          BusInRabbitMQ::class.java.simpleName to
            BusInRabbitMQ(
              factory,
              "testExchange",
              { it.value },
              { ObjTest(it) },
            ),
        )

      withData(list) { bus ->
        spyPing(exactly = 2, duration = 1.seconds) { ping ->
          bus.subscribe { obj ->
            ping()
            obj.value shouldStartWith "testMessage"
          }
          bus.publish(ObjTest("testMessage${Random.nextInt()}"))
          bus.publish(ObjTest("testMessage${Random.nextInt()}"))
        }
      }
    }
  })
