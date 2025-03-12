package eventDemo.libs.event

import kotlinx.datetime.Instant
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
 * @see EventStream
 */
interface Event<ID : AggregateId> {
    val eventId: UUID
    val aggregateId: ID
    val createdAt: Instant
    val version: Int
}
