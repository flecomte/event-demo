package eventDemo.configuration.injection

import eventDemo.adapter.infrastructureLayer.event.GameEventBusInMemory
import eventDemo.adapter.infrastructureLayer.event.GameEventStoreInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameListRepositoryInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameProjectionBusInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInMemory
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.gameList.GameListRepository
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.libs.event.projection.SnapshotConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.json.JsonObjectMapper

fun Module.configureDIInfrastructure(redisUrl: String) {
  factory {
    JedisPooled(redisUrl).apply {
      setJsonObjectMapper(
        object : JsonObjectMapper {
          override fun <T> fromJson(
            value: String,
            valueType: Class<T>,
          ): T {
            val s: KSerializer<T> = serializer(valueType) as KSerializer<T>
            return Json.decodeFromString(s, value)
          }

          override fun toJson(value: Any): String =
            Json.encodeToString(value)
        },
      )
    }
  } bind UnifiedJedis::class

  singleOf(::GameEventBusInMemory) bind GameEventBus::class
  singleOf(::GameEventStoreInMemory) bind GameEventStore::class
  singleOf(::GameProjectionBusInMemory) bind GameProjectionBus::class

  single {
    GameStateRepositoryInMemory(get(), get(), get(), snapshotConfig = SnapshotConfig())
  } bind GameStateRepository::class

  single {
    GameListRepositoryInMemory(get(), get(), get(), snapshotConfig = SnapshotConfig())
  } bind GameListRepository::class
}
