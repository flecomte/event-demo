package eventDemo.libs.event

class EventBusInMemory<E : Event<ID>, ID : AggregateId> : EventBus<E, ID> {
    private val subscribers: MutableList<Pair<Int, (E) -> Unit>> = mutableListOf()

    override fun publish(event: E) {
        subscribers
            .sortedByDescending { (priority, block) -> priority }
            .forEach { (_, block) ->
                block(event)
            }
    }

    override fun subscribe(
        priority: Int,
        block: (E) -> Unit,
    ) {
        subscribers.add(priority to block)
    }
}
