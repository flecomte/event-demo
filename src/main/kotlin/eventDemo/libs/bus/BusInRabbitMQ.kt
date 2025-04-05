package eventDemo.libs.bus

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.runBlocking

class BusInRabbitMQ<E>(
  private val connectionFactory: ConnectionFactory,
  private val queueName: String,
  private val objectToString: (E) -> String,
  private val stringToObject: (String) -> E,
) : Bus<E> {
  init {
    connectionFactory
      .newConnection()
      .createChannel()
      .use {
        it.queueDeclare(
          queueName,
          true,
          false,
          false,
          emptyMap(),
        )
      }
  }

  override suspend fun publish(item: E) {
    connectionFactory
      .newConnection()
      .createChannel()
      .use {
        it.basicPublish(
          "",
          queueName,
          null,
          objectToString(item).toByteArray(),
        )
      }
  }

  override fun subscribe(block: suspend (E) -> Unit) {
    connectionFactory
      .newConnection()
      .createChannel()
      .basicConsume(
        queueName,
        true,
        DeliverCallback { _: String, message: Delivery ->
          runBlocking {
            block(stringToObject(message.body.toString(Charsets.UTF_8)))
          }
        },
        CancelCallback {},
      )
  }
}
