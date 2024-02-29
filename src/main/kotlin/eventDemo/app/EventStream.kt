package eventDemo.app

import io.github.oshai.kotlinlogging.KotlinLogging

class EventStream<ID : AggregateId> {
    private val logger = KotlinLogging.logger {}
    private val eventBus: MutableMap<ID, MutableList<Event<ID>>> = mutableMapOf()

    fun publish(event: Event<ID>) {
        eventBus.getOrPut(event.id) { mutableListOf() }.add(event)
        logger.atInfo {
            message = "Event published: $event"
            payload = mapOf("event" to event)
        }
    }

    fun publish(vararg events: Event<ID>) {
        events.forEach { publish(it) }
    }

    fun <U : Event<ID>> read(
        aggregateId: ID,
        eventClass: Class<U>,
    ): U? = eventBus.get(aggregateId)?.filterIsInstance(eventClass)?.firstOrNull()
}

inline fun <reified U : Event<ID>, ID : AggregateId> EventStream<ID>.read(aggregateId: ID): U? = this.read(aggregateId, U::class.java)
