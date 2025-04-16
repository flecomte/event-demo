package eventDemo.libs.bus

interface Bus<T> {
  /**
   * Publish a new [message][item] to the bus.
   */
  fun publish(item: T)

  /**
   * Subscribe a [lambda][block] to the bus.
   *
   * When a message is sent to the bus, the [block] is executed.
   */
  fun subscribe(block: (T) -> Unit): Subscription

  /**
   * The returns of the [subscribe] method.
   * It can be called to [cancel][close] the subscription.
   */
  interface Subscription : AutoCloseable {
    override fun close()
  }
}
