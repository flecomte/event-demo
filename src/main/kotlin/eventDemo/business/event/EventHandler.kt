package eventDemo.business.event

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event

/**
 * A stream to publish and read the played card event.
 */
interface EventHandler<E : Event<ID>, ID : AggregateId> {
  fun registerProjectionBuilder(builder: (event: E) -> Unit)

  fun handle(
    aggregateId: ID,
    buildEvent: (version: Int) -> E,
  ): E
}
