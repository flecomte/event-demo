package eventDemo.libs.event

interface EventBus<E : Event<ID>, ID : AggregateId> {
    fun publish(event: E)

    fun subscribe(block: (E) -> Unit)
}
