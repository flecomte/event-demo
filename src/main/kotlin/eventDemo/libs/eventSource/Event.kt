package eventDemo.libs.eventSource

import eventDemo.libs.serializer.UUIDSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represent an ID for one aggregate, and it used in events
 * @see Event
 */
interface AggregateId {
  val id: UUID
}

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

@JvmInline
@Serializable
value class EventId(
  @Serializable(with = UUIDSerializer::class)
  val id: UUID = UUID.randomUUID(),
)
