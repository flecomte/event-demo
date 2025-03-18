package eventDemo.libs.bus

interface Bus<E> {
  fun publish(item: E)

  /**
   * @param priority The higher the priority, the more it will be called first
   */
  fun subscribe(
    priority: Int = 0,
    block: suspend (E) -> Unit,
  )
}
