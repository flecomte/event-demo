package eventDemo.libs.event

/**
 * Interface representing an event stream for publishing and reading domain events
 */
interface EventStream<E : Event<*>> {
  /** Publishes a single event to the event stream */
  fun publish(event: E)

  /** Publishes multiple events to the event stream */
  fun publish(vararg events: E)

  /** Reads all events */
  fun readAll(): Set<E>

  fun readGreaterOfVersion(version: Int): Set<E>

  fun readVersionBetween(version: IntRange): Set<E>
}
