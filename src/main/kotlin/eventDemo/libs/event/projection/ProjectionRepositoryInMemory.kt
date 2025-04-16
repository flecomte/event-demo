package eventDemo.libs.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import java.util.concurrent.ConcurrentHashMap

class ProjectionRepositoryInMemory<E : Event<ID>, P : Projection<ID>, ID : AggregateId>(
  private val initialStateBuilder: (aggregateId: ID) -> P,
  applyToProjection: P.(event: E) -> P,
) : ProjectionRepositoryAbs<E, P, ID>(applyToProjection),
  ProjectionRepository<E, P, ID> {
  private val projections: ConcurrentHashMap<ID, P> = ConcurrentHashMap()

  /**
   * Build the list of all [Projections][Projection]
   */
  override fun getList(
    limit: Int,
    offset: Int,
  ): List<P> =
    projections
      .values
      .drop(offset)
      .take(limit)

  /**
   * Get the [Projection].
   */
  override fun get(aggregateId: ID): P =
    projections[aggregateId]
      ?: initialStateBuilder(aggregateId)

  /**
   * Save the projection.
   */
  override fun save(projection: P) {
    projections.compute(projection.aggregateId) { id: ID, proj: P? ->
      val currentProjection = proj ?: initialStateBuilder(projection.aggregateId)
      if (currentProjection.lastEventVersion < projection.lastEventVersion) {
        projection
      } else {
        currentProjection
      }
    }
  }
}
