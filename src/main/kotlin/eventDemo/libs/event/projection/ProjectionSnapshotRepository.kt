package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event

interface ProjectionSnapshotRepository<E : Event<ID>, P : Projection<ID>, ID : AggregateId> {
  /**
   * Create a snapshot for the event
   */
  fun applyAndPutToCache(event: E): P

  /**
   * Build the list of all [Projections][Projection]
   */
  fun getList(): List<P>

  /**
   * Build the last version of the [Projection] from the cache.
   */
  fun getLast(aggregateId: ID): P

  /**
   * Build the [Projection] to the specific [event][Event].
   *
   * It does not contain the [events][Event] it after this one.
   */
  fun getUntil(event: E): P
}
