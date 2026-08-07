package eventDemo.libs.bus

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlin.reflect.KClass

class BusInMemory<E>(
  val name: KClass<*> = BusInMemory::class,
) : Bus<E> {
  private val logger = KotlinLogging.logger(name.qualifiedName.toString())
  private val subscribers: MutableList<(E) -> Unit> = mutableListOf()

  override fun publish(item: E) {
    withLoggingContext("busItem" to item.toString()) {
      logger.info { "Item sent to the bus" }
      subscribers
        .forEach {
          it(item)
        }
    }
  }

  override fun subscribe(block: (E) -> Unit): Bus.Subscription {
    subscribers.add(block)
    return object : Bus.Subscription {
      override fun close() {
        subscribers.remove(block)
      }
    }
  }
}
