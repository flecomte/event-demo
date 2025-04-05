package eventDemo.libs.bus

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

class BusInMemory<E>(
  val name: KClass<*> = BusInMemory::class,
) : Bus<E> {
  private val logger = KotlinLogging.logger(name.qualifiedName.toString())
  private val subscribers: MutableList<suspend (E) -> Unit> = mutableListOf()

  override suspend fun publish(item: E) {
    withLoggingContext("busItem" to item.toString()) {
      logger.info { "Item sent to the bus: $item" }
      subscribers
        .forEach {
          coroutineScope {
            it(item)
          }
        }
    }
  }

  override fun subscribe(block: suspend (E) -> Unit) {
    subscribers.add(block)
  }
}
