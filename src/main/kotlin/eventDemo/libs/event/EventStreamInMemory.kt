package eventDemo.libs.event

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KClass

abstract class EventStreamInMemory<E : Event<ID>, ID : AggregateId>(
    private val eventType: Class<E>,
) : EventStream<E, ID> {
    private val logger = KotlinLogging.logger {}
    private val eventBus: MutableList<E> = mutableListOf()

    override fun publish(event: E) {
        eventBus.add(event)
        logger.atInfo {
            message = "Event published: $event"
            payload = mapOf("event" to event)
        }
    }

    override fun publish(vararg events: E) {
        events.forEach { publish(it) }
    }

    override fun readLast(aggregateId: ID): E? = eventBus.lastOrNull()

    override fun <R : E> readLastOf(
        aggregateId: ID,
        eventType: KClass<out R>,
    ): R? =
        eventBus
            .filterIsInstance(eventType.java)
            .lastOrNull { it.id == aggregateId }

    override fun readAll(aggregateId: ID): Flow<E> =
        flow {
            eventBus.forEach { emit(it) }
        }
}

inline fun <reified R : E, E : Event<ID>, ID : AggregateId> EventStreamInMemory<E, ID>.readLastOf(aggregateId: ID): R? =
    readLastOf(aggregateId, R::class)
