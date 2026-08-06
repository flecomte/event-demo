package eventDemo.contexts.game.intrastructure.persistence.connectors

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import eventDemo.Tag
import eventDemo.testHelpers.spyPing
import eventDemo.testHelpers.testKoinApplicationWithConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldStartWith
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class RabbitMQTest :
  FunSpec({
    tags(Tag.RabbitMQ)

    test("test connection with RabbitMQ") {
      testKoinApplicationWithConfig {
        val exchangeName = "test_" + UUID.randomUUID()
        get<ConnectionFactory>().newConnection().use { connection ->
          connection
            .createChannel()
            .use { channel ->
              channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT)
              val queue = channel.queueDeclare("myQueue", true, false, false, emptyMap()).queue
              channel.queueBind(queue, exchangeName, "")

              spyPing(3.seconds, exactly = 2) { ping ->
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
                        ping()
                        channel.basicAck(envelope.deliveryTag, false)
                      }
                    },
                  )

                channel.basicPublish(exchangeName, "", BasicProperties(), "testMessage1".toByteArray())
                channel.basicPublish(exchangeName, "", BasicProperties(), "testMessage2".toByteArray())
              }

              channel.queueDelete(queue)
              channel.exchangeDelete(exchangeName)
            }
        }
      }
    }
  })
