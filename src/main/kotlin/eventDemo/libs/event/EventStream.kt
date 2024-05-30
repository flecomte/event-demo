package eventDemo.libs.event

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface EventStream<E : Event<ID>, ID : AggregateId> {
    fun publish(event: E)

    fun publish(vararg events: E)

    fun readLast(aggregateId: ID): E?

    fun <R : E> readLastOf(
        aggregateId: ID,
        eventType: KClass<out R>,
    ): E?

    fun readAll(aggregateId: ID): Flow<E>
}
