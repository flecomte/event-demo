package eventDemo.libs.event

import kotlin.reflect.KClass

/**
 * Interface representing an event stream for publishing and reading domain events
 */
interface EventStream<E : Event<ID>, ID : AggregateId> {
    /** Publishes a single event to the event stream */
    fun publish(event: E)

    /** Publishes multiple events to the event stream */
    fun publish(vararg events: E)

    /** Reads the last event associated with a given aggregate ID */
    fun readLast(aggregateId: ID): E?

    /** Reads the last event of a specific type associated with a given aggregate ID */
    fun <R : E> readLastOf(
        aggregateId: ID,
        eventType: KClass<out R>,
    ): R?

    /** Reads all events associated with a given aggregate ID */
    fun readAll(aggregateId: ID): List<E>
}
