package eventDemo.configuration.injection

import eventDemo.adapter.infrastructureLayer.event.GameEventBusInMemory
import eventDemo.adapter.infrastructureLayer.event.GameEventStoreInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameListRepositoryInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameProjectionBusInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInRedis
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.gameList.GameListRepository
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.libs.event.projection.SnapshotConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.UnifiedJedis

fun Module.configureDIInfrastructure(redisUrl: String) {
  factory {
    JedisPooled(redisUrl)
  } bind UnifiedJedis::class

  singleOf(::GameEventBusInMemory) bind GameEventBus::class
  singleOf(::GameEventStoreInMemory) bind GameEventStore::class
  singleOf(::GameProjectionBusInMemory) bind GameProjectionBus::class

  single {
    GameStateRepositoryInRedis(get(), get(), get(), get(), snapshotConfig = SnapshotConfig())
  } bind GameStateRepository::class

  single {
    GameListRepositoryInMemory(get(), get(), get(), snapshotConfig = SnapshotConfig())
  } bind GameListRepository::class
}
