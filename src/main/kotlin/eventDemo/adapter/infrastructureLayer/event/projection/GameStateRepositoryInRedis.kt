package eventDemo.adapter.infrastructureLayer.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.event.GameEvent
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.gameState.GameState
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.business.event.projection.gameState.apply
import eventDemo.libs.event.projection.ProjectionSnapshotRepositoryInRedis
import eventDemo.libs.event.projection.SnapshotConfig
import kotlinx.serialization.json.Json
import redis.clients.jedis.UnifiedJedis

/**
 * Manages [projections][GameState], their building and publication in the [bus][GameProjectionBus].
 */
class GameStateRepositoryInRedis(
  eventStore: GameEventStore,
  projectionBus: GameProjectionBus,
  eventBus: GameEventBus,
  jedis: UnifiedJedis,
  snapshotConfig: SnapshotConfig = SnapshotConfig(),
) : GameStateRepository {
  private val projectionsSnapshot =
    ProjectionSnapshotRepositoryInRedis(
      eventStore = eventStore,
      snapshotCacheConfig = snapshotConfig,
      initialStateBuilder = { aggregateId: GameId -> GameState(aggregateId) },
      projectionClass = GameState::class,
      projectionToJson = { Json.encodeToString(GameState.serializer(), it) },
      jsonToProjection = { Json.decodeFromString(GameState.serializer(), it) },
      applyToProjection = GameState::apply,
      jedis = jedis,
    )

  init {
    // On new event was received, build snapshot and publish it to the projection bus
    eventBus.subscribe { event ->
      projectionsSnapshot
        .applyAndPutToCache(event)
        .also { projectionBus.publish(it) }
    }
  }

  /**
   * Get the last version of the [GameState] from the all eventStream.
   *
   * It fetches it from the local cache if possible, otherwise it builds it.
   */
  override fun getLast(gameId: GameId): GameState =
    projectionsSnapshot.getLast(gameId)

  /**
   * Get the [GameState] to the specific [event][GameEvent].
   * It does not contain the [events][GameEvent] it after this one.
   *
   * It fetches it from the local cache if possible, otherwise it builds it.
   */
  override fun getUntil(event: GameEvent): GameState =
    projectionsSnapshot.getUntil(event)
}
