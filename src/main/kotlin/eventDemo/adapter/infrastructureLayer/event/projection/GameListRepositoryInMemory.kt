package eventDemo.adapter.infrastructureLayer.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.projection.GameList
import eventDemo.business.event.projection.GameListRepository
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.GameState
import eventDemo.business.event.projection.apply
import eventDemo.libs.event.projection.ProjectionRepositoryInMemory
import io.github.oshai.kotlinlogging.withLoggingContext

/**
 * Manages [projections][GameList], their building and publication in the [bus][GameProjectionBus].
 */
class GameListRepositoryInMemory : GameListRepository {
  private val projectionsRepository =
    ProjectionRepositoryInMemory(
      applyToProjection = GameList::apply,
      initialStateBuilder = { aggregateId: GameId -> GameList(aggregateId) },
    )

  fun subscribeToBus(
    projectionBus: GameProjectionBus,
    eventBus: GameEventBus,
  ) {
    // On new event was received, build projection and publish it to the projection bus
    eventBus.subscribe { event ->
      withLoggingContext("event" to event.toString()) {
        projectionsRepository
          .applyAndSave(event)
          .also { projectionBus.publish(it) }
      }
    }
  }

  /**
   * Get the last version of the [GameState] from the all eventStream.
   *
   * It fetches it from the local cache if possible, otherwise it builds it.
   */
  override fun getList(): List<GameList> =
    projectionsRepository.getList()
}
