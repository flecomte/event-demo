package eventDemo.libs.event

import eventDemo.libs.event.projection.Projection
import io.github.oshai.kotlinlogging.withLoggingContext

/**
 * Interface representing an event stream for publishing and reading domain events
 */
interface EventStream<E : Event<ID>, ID : AggregateId> {
  val aggregateId: ID

  /** Publishes a single event to the event stream */
  fun publish(event: E)

  /** Publishes multiple events to the event stream */
  fun publish(vararg events: E) {
    events.forEach {
      withLoggingContext("event" to it.toString()) {
        publish(it)
      }
    }
  }

  /** Reads all events */
  fun readAll(): Set<E>

  fun readGreaterOfVersion(version: Int): Set<E> =
    readVersionBetween(version + 1..Int.MAX_VALUE)

  fun readVersionBetween(version: IntRange): Set<E>

  fun <P : Projection<*>> readVersionBetween(
    projection: P?,
    event: E,
  ): Set<E> =
    readVersionBetween(((projection?.lastEventVersion ?: 0) + 1)..event.version)

  fun getByVersion(version: Int): E? =
    readVersionBetween(version..version).firstOrNull()
}
