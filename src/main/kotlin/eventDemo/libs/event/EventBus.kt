package eventDemo.libs.event

interface EventBus<E : Event<ID>, ID : AggregateId> {
  fun publish(event: E)

  /**
   * @param priority The higher the priority, the more it will be called first
   */
  fun subscribe(
    priority: Int = 0,
    block: suspend (E) -> Unit,
  )
}
