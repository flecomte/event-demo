package eventDemo.libs.event

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * An In-Memory implementation of an event stream.
 *
 * All methods are implemented.
 */
class EventStreamInMemory<E : Event<*>> : EventStream<E> {
    private val logger = KotlinLogging.logger {}
    private val events: Queue<E> = ConcurrentLinkedQueue()

    override fun publish(event: E) {
        if (events.none { it.eventId == event.eventId }) {
            events.add(event)
            logger.atInfo {
                message = "Event published: $event"
                payload = mapOf("event" to event)
            }
        }
    }

    override fun publish(vararg events: E) {
        events.forEach { publish(it) }
    }

    override fun readAll(): Set<E> = events.toSet()

    override fun readGreaterOfVersion(version: Int): Set<E> =
        events
            .filter { it.version > version }
            .toSet()

    override fun readVersionBetween(version: IntRange): Set<E> =
        events
            .filter { version.contains(it.version) }
            .toSet()
}
