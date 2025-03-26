package eventDemo.libs.bus

import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.coroutineScope

class BusInMemory<E> : Bus<E> {
  private val subscribers: MutableList<Pair<Int, suspend (E) -> Unit>> = mutableListOf()

  override suspend fun publish(item: E) {
    withLoggingContext("busItem" to item.toString()) {
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
