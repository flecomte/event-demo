package eventDemo.libs.bus

import com.rabbitmq.client.ConnectionFactory
import io.kotest.assertions.nondeterministic.until
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.equals.shouldBeEqual
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
          virtualHost = virtualHost
          username = "event-demo"
          password = "changeit"
        }
      val list: Map<String, Bus<ObjTest>> =
        mapOf(
          BusInMemory::class.java.simpleName to BusInMemory(),
          BusInRabbitMQ::class.java.simpleName to
            BusInRabbitMQ(
              factory,
              "testQueue",
              { it.value },
              { ObjTest(it) },
            ),
        )

      withData(list) { bus ->
        val value = "hello${Random.nextInt()}"
        var isCalled = false

        bus.subscribe { obj ->
          isCalled = true
          obj.value shouldBeEqual value
        }
        bus.publish(ObjTest(value))

        until(3.seconds) {
          isCalled shouldBeEqual true
        }
      }
    }
  })
