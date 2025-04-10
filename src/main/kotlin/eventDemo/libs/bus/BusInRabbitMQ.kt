package eventDemo.libs.bus

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking

class BusInRabbitMQ<E>(
  private val connectionFactory: ConnectionFactory,
  private val exchangeName: String,
  private val objectToString: (E) -> String,
  private val stringToObject: (String) -> E,
) : Bus<E> {
  private val connection: Connection = connectionFactory.newConnection()
    get() {
      return if (field.isOpen) {
        field
      } else {
        connectionFactory.newConnection()
      }
    }
  private val routingKey = ""

  init {
    connection
      .createChannel()
      .use {
        it.exchangeDeclare(
          exchangeName,
          BuiltinExchangeType.FANOUT,
          true,
          false,
          emptyMap(),
        )
      }
  }

  override suspend fun publish(item: E) {
    connection
      .createChannel()
      .use {
        it.basicPublish(
          exchangeName,
          routingKey,
          AMQP.BasicProperties(),
          objectToString(item).toByteArray(),
        )
      }
  }

  override fun subscribe(block: suspend (E) -> Unit): Bus.Subscription {
    connection
      .createChannel()
      .also { channel ->
        val queue =
          channel
            .queueDeclare()
            .queue
            .also { channel.queueBind(it, exchangeName, routingKey) }

        channel
          .basicConsume(
            queue,
            object : DefaultConsumer(channel) {
              override fun handleDelivery(
                consumerTag: String,
                envelope: Envelope,
                properties: AMQP.BasicProperties,
                body: ByteArray,
              ) {
                runBlocking {
                  block(stringToObject(body.toString(Charsets.UTF_8)))
                }
                channel.basicAck(envelope.deliveryTag, false)
              }
            },
          )
      }.let {
        return object : Bus.Subscription {
          override fun close() {
            it.close()
          }
        }
      }
  }
}
