package eventDemo.libs.bus

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.coroutineScope
import kotlin.reflect.KClass

class BusInMemory<E>(
  val name: KClass<*> = BusInMemory::class,
) : Bus<E> {
  private val logger = KotlinLogging.logger(name.qualifiedName.toString())
  private val subscribers: MutableList<Pair<Int, suspend (E) -> Unit>> = mutableListOf()

  override suspend fun publish(item: E) {
    withLoggingContext("busItem" to item.toString()) {
      logger.info { "Item sent to the bus: $item" }
      subscribers
        .sortedByDescending { (priority, _) -> priority }
        .forEach { (_, block) ->
          coroutineScope {
            block(item)
          }
        }
    }
  }

  override fun subscribe(
    priority: Int,
    block: suspend (E) -> Unit,
  ) {
    subscribers.add(priority to block)
  }
}
