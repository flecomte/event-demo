package eventDemo.libs.event

class EventBusInMemory<E : Event<ID>, ID : AggregateId> : EventBus<E, ID> {
    private val subscribers: MutableList<(E) -> Unit> = mutableListOf()

    override fun publish(event: E) {
        subscribers.forEach {
            it(event)
        }
    }

    override fun subscribe(block: (E) -> Unit) {
        subscribers.add(block)
    }
}
