package eventDemo.libs.bus

interface Bus<T> {
  suspend fun publish(item: T)

  /**
   * @param priority The higher the priority, the more it will be called first
   */
  fun subscribe(
    priority: Int = 0,
    block: suspend (T) -> Unit,
  )
}
