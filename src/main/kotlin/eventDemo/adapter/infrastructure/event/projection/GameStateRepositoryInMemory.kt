package eventDemo.adapter.infrastructure.event.projection

import eventDemo.domain.entity.GameId
import eventDemo.domain.event.GameEventBus
import eventDemo.domain.event.projection.GameProjectionBus
import eventDemo.domain.event.projection.GameState
import eventDemo.domain.event.projection.GameStateRepository
import eventDemo.domain.event.projection.apply
import eventDemo.libs.event.projection.ProjectionRepositoryInMemory
import io.github.oshai.kotlinlogging.withLoggingContext

/**
 * Manages [projections][GameState], their building and publication in the [bus][GameProjectionBus].
 */
class GameStateRepositoryInMemory : GameStateRepository {
  private val projectionsRepository =
    ProjectionRepositoryInMemory(
      applyToProjection = GameState::apply,
      initialStateBuilder = { aggregateId: GameId -> GameState(aggregateId) },
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
   * Get the [GameState].
   */
  override fun get(gameId: GameId): GameState =
    projectionsRepository.get(gameId)
}
