package eventDemo.libs.event

interface EventStore<E : Event<ID>, ID : AggregateId> {
  fun getStream(aggregateId: ID): EventStream<E>

  fun publish(event: E)
}
