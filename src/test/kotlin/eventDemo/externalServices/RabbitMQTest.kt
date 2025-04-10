package eventDemo.externalServices

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import eventDemo.Tag
import eventDemo.testKoinApplicationWithConfig
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldStartWith
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class RabbitMQTest :
  FunSpec({
    tags(Tag.RabbitMQ)

    test("test connection with RabbitMQ") {
      testKoinApplicationWithConfig {
        val factory = get<ConnectionFactory>()

        val exchangeName = "test_" + UUID.randomUUID()

        val spy = spyk(mockk<() -> Unit>())

        factory.newConnection().use { connection ->
          connection
            .createChannel()
            .use { channel ->
              channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT)
              val queue = channel.queueDeclare("qqq", true, false, false, emptyMap()).queue
              channel.queueBind(queue, exchangeName, "")

              channel
                .basicConsume(
                  queue,
                  object : DefaultConsumer(channel) {
                    override fun handleDelivery(
                      consumerTag: String,
                      envelope: Envelope,
                      properties: BasicProperties,
                      body: ByteArray,
                    ) {
                      val msg = body.toString(Charsets.UTF_8)
                      msg shouldStartWith "testMessage"
                      spy()
                      channel.basicAck(envelope.deliveryTag, false)
                    }
                  },
                )

              channel.basicPublish(exchangeName, "", BasicProperties(), "testMessage1".toByteArray())
              channel.basicPublish(exchangeName, "", BasicProperties(), "testMessage2".toByteArray())

              eventually(3.seconds) {
                verify(exactly = 2) { spy() }
              }

              channel.queueDelete(queue)
              channel.exchangeDelete(exchangeName)
            }
        }
      }
    }
  })
