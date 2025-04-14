package eventDemo.adapter.infrastructureLayer.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.GameList
import eventDemo.business.event.projection.GameListRepository
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.GameState
import eventDemo.business.event.projection.apply
import eventDemo.libs.event.projection.ProjectionSnapshotRepositoryInMemory
import eventDemo.libs.event.projection.SnapshotConfig
import io.github.oshai.kotlinlogging.withLoggingContext

/**
 * Manages [projections][GameList], their building and publication in the [bus][GameProjectionBus].
 */
class GameListRepositoryInMemory(
  eventStore: GameEventStore,
  snapshotConfig: SnapshotConfig = SnapshotConfig(),
) : GameListRepository {
  private val projectionsSnapshot =
    ProjectionSnapshotRepositoryInMemory(
      name = GameListRepositoryInMemory::class,
      eventStore = eventStore,
      snapshotCacheConfig = snapshotConfig,
      applyToProjection = GameList::apply,
      initialStateBuilder = { aggregateId: GameId -> GameList(aggregateId) },
    )

  fun subscribeToBus(
    projectionBus: GameProjectionBus,
    eventBus: GameEventBus,
  ) {
    // On new event was received, build snapshot and publish it to the projection bus
    eventBus.subscribe { event ->
      withLoggingContext("event" to event.toString()) {
        projectionsSnapshot
          .applyAndPutToCache(event)
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
    projectionsSnapshot.getList()
}
