package eventDemo.libs.event

import io.github.oshai.kotlinlogging.withLoggingContext

interface EventStore<E : Event<ID>, ID : AggregateId> {
  fun getStream(aggregateId: ID): EventStream<E, ID>

  fun publish(event: E) =
    withLoggingContext("event" to event.toString()) {
      getStream(event.aggregateId).publish(event)
    }
}
