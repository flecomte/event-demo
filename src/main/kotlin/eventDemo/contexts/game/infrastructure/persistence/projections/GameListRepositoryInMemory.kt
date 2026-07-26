package eventDemo.contexts.game.infrastructure.persistence.projections

import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.application.ports.GameEventStore
import eventDemo.contexts.game.application.ports.GameProjectionBus
import eventDemo.contexts.game.application.projections.applyEvent
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.infrastructure.persistence.projections.models.GameList
import eventDemo.domain.event.projection.GameListRepository
import io.github.oshai.kotlinlogging.withLoggingContext

/**
 * Manages [projections][GameList], their building and publication in the [bus][GameProjectionBus].
 */
class GameListRepositoryInMemory(
  val gameEventStore: GameEventStore,
  val projectionBus: GameProjectionBus,
  val eventBus: GameEventBus,
) : GameListRepository {
  val projections: MutableMap<GameId, GameList> = mutableMapOf()

  override fun getList(
    limit: Int,
    offset: Int,
  ): List<GameList> =
    projections
      .values
      .drop(offset)
      .take(limit)

  override fun save(gameList: GameList) {
    projections[gameList.aggregateId] = gameList
  }

  override fun subscribeToBus() {
    // On new event was received, build projection and publish it to the projection bus
    eventBus.subscribe { event ->
      withLoggingContext("event" to event.toString()) {
        gameEventStore
          .getStream(event.aggregateId)
          .readAll()
          .fold(GameList(event.aggregateId)) { acc, event ->
            acc.applyEvent(event)
          }.also { save(it) }
          .also { projectionBus.publish(it) }
      }
    }
  }
}
