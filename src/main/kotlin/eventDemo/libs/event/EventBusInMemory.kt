package eventDemo.libs.event

import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.runBlocking

class EventBusInMemory<E : Event<ID>, ID : AggregateId> : EventBus<E, ID> {
  private val subscribers: MutableList<Pair<Int, suspend (E) -> Unit>> = mutableListOf()

  override fun publish(event: E) {
    subscribers
      .sortedByDescending { (priority, _) -> priority }
      .forEach { (_, block) ->
        runBlocking {
          withLoggingContext("event" to event.toString()) {
            block(event)
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
