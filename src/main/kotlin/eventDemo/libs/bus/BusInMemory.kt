package eventDemo.libs.bus

import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.runBlocking

class BusInMemory<E> : Bus<E> {
  private val subscribers: MutableList<Pair<Int, suspend (E) -> Unit>> = mutableListOf()

  override fun publish(item: E) {
    subscribers
      .sortedByDescending { (priority, _) -> priority }
      .forEach { (_, block) ->
        runBlocking {
          withLoggingContext("busItem" to item.toString()) {
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
