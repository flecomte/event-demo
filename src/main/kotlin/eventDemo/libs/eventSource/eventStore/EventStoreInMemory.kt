package eventDemo.libs.eventSource.eventStore

import eventDemo.libs.eventSource.AggregateId
import eventDemo.libs.eventSource.Event
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EventStoreInMemory<E : Event<ID>, ID : AggregateId> : EventStore<E, ID> {
  private val streams: ConcurrentMap<ID, EventStream<E, ID>> = ConcurrentHashMap()

  override fun getStream(aggregateId: ID): EventStream<E, ID> =
    streams.computeIfAbsent(aggregateId) { EventStreamInMemory(aggregateId) }
}
