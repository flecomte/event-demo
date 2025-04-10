package eventDemo.libs.bus

import com.rabbitmq.client.ConnectionFactory
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.string.shouldStartWith
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
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
        val spy = spyk(mockk<() -> Unit>())

        bus.subscribe { obj ->
          spy()
          obj.value shouldStartWith "testMessage"
        }
        bus.publish(ObjTest("testMessage${Random.nextInt()}"))
        bus.publish(ObjTest("testMessage${Random.nextInt()}"))

        eventually(1.seconds) {
          verify(exactly = 2) { spy() }
        }
      }
    }
  })
