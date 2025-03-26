package eventDemo.libs.event

import io.github.oshai.kotlinlogging.withLoggingContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EventStoreInMemory<E : Event<ID>, ID : AggregateId> : EventStore<E, ID> {
  private val streams: ConcurrentMap<ID, EventStream<E>> = ConcurrentHashMap()

  override fun getStream(aggregateId: ID): EventStream<E> =
    streams.computeIfAbsent(aggregateId) { EventStreamInMemory() }

  override fun publish(event: E) =
    withLoggingContext("event" to event.toString()) {
      getStream(event.aggregateId).publish(event)
    }
}
