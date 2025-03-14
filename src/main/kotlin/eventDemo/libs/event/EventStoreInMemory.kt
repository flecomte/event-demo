package eventDemo.libs.event

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class EventStoreInMemory<E : Event<ID>, ID : AggregateId> : EventStore<E, ID> {
    private val streams: ConcurrentMap<ID, EventStream<E>> = ConcurrentHashMap()

    override fun getStream(aggregateId: ID): EventStream<E> = streams.computeIfAbsent(aggregateId) { EventStreamInMemory() }

    override fun publish(event: E) = getStream(event.aggregateId).publish(event)
}
