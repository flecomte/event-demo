package eventDemo.adapter.infrastructureLayer.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.gameList.GameList
import eventDemo.business.event.projection.gameList.GameListRepository
import eventDemo.business.event.projection.gameList.apply
import eventDemo.business.event.projection.gameState.GameState
import eventDemo.libs.event.projection.ProjectionSnapshotRepositoryInMemory
import eventDemo.libs.event.projection.SnapshotConfig

class GameListRepositoryInMemory(
  eventStore: GameEventStore,
  eventHandler: GameEventHandler,
  snapshotConfig: SnapshotConfig = SnapshotConfig(),
) : GameListRepository {
  private val projectionsSnapshot =
    ProjectionSnapshotRepositoryInMemory(
      eventStore = eventStore,
      snapshotCacheConfig = snapshotConfig,
      applyToProjection = GameList::apply,
      initialStateBuilder = { aggregateId: GameId -> GameList(aggregateId) },
    )

  init {
    eventHandler.registerProjectionBuilder { event ->
      projectionsSnapshot.applyAndPutToCache(event)
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
