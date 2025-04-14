package eventDemo.configuration.injection

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import eventDemo.adapter.infrastructureLayer.event.GameEventBusInRabbinMQ
import eventDemo.adapter.infrastructureLayer.event.GameEventStoreInPostgresql
import eventDemo.adapter.infrastructureLayer.event.projection.GameListRepositoryInRedis
import eventDemo.adapter.infrastructureLayer.event.projection.GameProjectionBusInRabbitMQ
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInRedis
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.GameListRepository
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.GameStateRepository
import eventDemo.libs.event.projection.SnapshotConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeCallback
import org.koin.dsl.bind
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.UnifiedJedis
import javax.sql.DataSource

fun Module.configureDIInfrastructure(config: Configuration) {
  // Postgresql config
  single {
    JedisPooled(config.redisUrl)
  } bind UnifiedJedis::class

  single {
    HikariConfig()
      .apply {
        jdbcUrl = config.postgresql.url
        username = config.postgresql.username
        password = config.postgresql.password
        maximumPoolSize = 10
        minimumIdle = 10
      }.let {
        HikariDataSource(it)
      }.also { datasource ->
        registerCallback(
          object : ScopeCallback {
            override fun onScopeClose(scope: Scope) {
              datasource.close()
            }
          },
        )
      }
  } bind DataSource::class

  // RabbitMQ config
  factory {
    ConnectionFactory().apply {
      host = config.rabbitmq.url
      port = config.rabbitmq.port
      username = config.rabbitmq.username
      password = config.rabbitmq.password
    }
  }

  singleOf(::GameEventBusInRabbinMQ) bind GameEventBus::class
  singleOf(::GameEventStoreInPostgresql) bind GameEventStore::class
  singleOf(::GameProjectionBusInRabbitMQ) bind GameProjectionBus::class

  single {
    GameStateRepositoryInRedis(get(), get(), snapshotConfig = SnapshotConfig())
  } bind GameStateRepository::class

  single {
    GameListRepositoryInRedis(get(), get(), snapshotConfig = SnapshotConfig())
  } bind GameListRepository::class
}
