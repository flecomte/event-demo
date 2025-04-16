package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event

interface ProjectionRepository<E : Event<ID>, P : Projection<ID>, ID : AggregateId> {
  /**
   * Update projection with the event.
   */
  fun apply(event: E): P

  /**
   * Update projection with the event, and save it.
   */
  fun applyAndSave(event: E): P

  /**
   * Save the projection.
   */
  fun save(projection: P)

  /**
   * Build the list of all [Projections][Projection]
   */
  fun getList(
    limit: Int = 100,
    offset: Int = 0,
  ): List<P>

  /**
   * Build the last version of the [Projection] from the cache.
   */
  fun get(aggregateId: ID): P
}
