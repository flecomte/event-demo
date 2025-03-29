package eventDemo.libs.event

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EventStoreInMemory<E : Event<ID>, ID : AggregateId> : EventStore<E, ID> {
  private val streams: ConcurrentMap<ID, EventStream<E, ID>> = ConcurrentHashMap()

  override fun getStream(aggregateId: ID): EventStream<E, ID> =
    streams.computeIfAbsent(aggregateId) { EventStreamInMemory(aggregateId) }
}
