package eventDemo.libs.eventSource

import eventDemo.shared.ids.AggregateId
import eventDemo.shared.ids.EventId
import kotlinx.datetime.Instant

/**
 * The basic interface for an Event
 * @see eventDemo.libs.eventSource.eventStore.EventStream
 */
interface Event<ID : AggregateId> {
  val eventId: EventId
  val aggregateId: ID
  val createdAt: Instant
  val version: Int
}
