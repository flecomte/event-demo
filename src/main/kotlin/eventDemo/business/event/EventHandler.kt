package eventDemo.business.event

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event

/**
 * A stream to publish and read the played card event.
 */
interface EventHandler<E : Event<ID>, ID : AggregateId> {
  suspend fun handle(
    aggregateId: ID,
    buildEvent: (version: Int) -> E,
  ): E
}
