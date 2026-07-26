package eventDemo.libs.eventSource.eventStore

import eventDemo.libs.eventSource.AggregateId
import eventDemo.libs.eventSource.Event
import io.github.oshai.kotlinlogging.withLoggingContext

/**
 * Interface representing an event stream for publishing and reading domain events
 */
interface EventStream<E : Event<ID>, ID : AggregateId> {
  val aggregateId: ID

  /** Publishes a single event to the event stream */
  @Throws(VersionConflictException::class)
  fun append(event: E)

  /** Publishes multiple events to the event stream */
  fun append(vararg events: E) {
    events.forEach {
      withLoggingContext("event" to it.toString()) {
        append(it)
      }
    }
  }

  /** Reads all events */
  fun readAll(): Set<E>

  fun readGreaterOfVersion(version: Int): Set<E> =
    readVersionBetween(version + 1..Int.MAX_VALUE)

  fun readVersionBetween(version: IntRange): Set<E>

  fun getByVersion(version: Int): E? =
    readVersionBetween(version..version).firstOrNull()

  fun exist(): Boolean
}

class VersionConflictException(
  event: Event<*>,
) : RuntimeException("Version conflict: ${event.version}")
