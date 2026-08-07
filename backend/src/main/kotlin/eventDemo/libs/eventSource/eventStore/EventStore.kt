package eventDemo.libs.eventSource.eventStore

import eventDemo.libs.eventSource.Event
import eventDemo.shared.ids.AggregateId
import io.github.oshai.kotlinlogging.withLoggingContext

interface EventStore<E : Event<ID>, ID : AggregateId> {
  fun getStream(aggregateId: ID): EventStream<E, ID>

  @Throws(VersionConflictException::class)
  fun append(event: E) =
    withLoggingContext("event" to event.toString()) {
      getStream(event.aggregateId).append(event)
    }

  @Throws(VersionConflictException::class)
  fun append(events: Set<E>) =
    events.forEach { append(it) }
}
