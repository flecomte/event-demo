package eventDemo.libs.event

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass

/**
 * An In-Memory implementation of an event stream.
 *
 * All methods are implemented.
 */
class EventStreamInMemory<E : Event<ID>, ID : AggregateId> : EventStream<E, ID> {
    private val logger = KotlinLogging.logger {}
    private val events: MutableList<E> = mutableListOf()

    override fun publish(event: E) {
        events.add(event)
        logger.atInfo {
            message = "Event published: $event"
            payload = mapOf("event" to event)
        }
    }

    override fun publish(vararg events: E) {
        events.forEach { publish(it) }
    }

    override fun readLast(aggregateId: ID): E? = events.lastOrNull()

    override fun <R : E> readLastOf(
        aggregateId: ID,
        eventType: KClass<out R>,
    ): R? =
        events
            .filterIsInstance(eventType.java)
            .lastOrNull { it.gameId == aggregateId }

    override fun readAll(aggregateId: ID): List<E> = events
}

inline fun <reified R : E, E : Event<ID>, ID : AggregateId> EventStream<E, ID>.readLastOf(aggregateId: ID): R? =
    readLastOf(aggregateId, R::class)
