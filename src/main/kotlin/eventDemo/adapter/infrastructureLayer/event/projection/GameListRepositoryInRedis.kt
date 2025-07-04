package eventDemo.adapter.infrastructureLayer.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.GameList
import eventDemo.business.event.projection.GameListRepository
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.GameState
import eventDemo.business.event.projection.apply
import eventDemo.libs.event.projection.ProjectionSnapshotRepositoryInRedis
import eventDemo.libs.event.projection.SnapshotConfig
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.serialization.json.Json
import redis.clients.jedis.UnifiedJedis

/**
 * Manages [projections][GameList], their building and publication in the [bus][GameProjectionBus].
 */
class GameListRepositoryInRedis(
  eventStore: GameEventStore,
  jedis: UnifiedJedis,
  snapshotConfig: SnapshotConfig = SnapshotConfig(),
) : GameListRepository {
  private val projectionsSnapshot =
    ProjectionSnapshotRepositoryInRedis(
      eventStore = eventStore,
      snapshotCacheConfig = snapshotConfig,
      initialStateBuilder = { aggregateId: GameId -> GameList(aggregateId) },
      projectionClass = GameList::class,
      projectionToJson = { Json.encodeToString(GameList.serializer(), it) },
      jsonToProjection = { Json.decodeFromString(GameList.serializer(), it) },
      applyToProjection = GameList::apply,
      jedis = jedis,
    )

  fun subscribeToBus(
    projectionBus: GameProjectionBus,
    eventBus: GameEventBus,
  ) {
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
