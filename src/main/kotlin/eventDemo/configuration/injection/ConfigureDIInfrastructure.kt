package eventDemo.configuration.injection

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import eventDemo.adapter.infrastructureLayer.event.GameEventBusInRabbinMQ
import eventDemo.adapter.infrastructureLayer.event.GameEventStoreInPostgresql
import eventDemo.adapter.infrastructureLayer.event.projection.GameListRepositoryInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameProjectionBusInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInMemory
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.business.event.projection.gameList.GameListRepository
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.libs.event.projection.SnapshotConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import javax.sql.DataSource

fun Module.configureDIInfrastructure(config: Configuration) {
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
      }
  } bind DataSource::class

  single {
    ConnectionFactory().apply {
      host = config.rabbitmq.url
      port = config.rabbitmq.port
      virtualHost = virtualHost
      username = config.rabbitmq.username
      password = config.rabbitmq.password
    }
  }
  singleOf(::GameEventBusInRabbinMQ) bind GameEventBus::class
  singleOf(::GameEventStoreInPostgresql) bind GameEventStore::class
  singleOf(::GameProjectionBusInMemory) bind GameProjectionBus::class

  single {
    GameStateRepositoryInMemory(get(), snapshotConfig = SnapshotConfig())
  } bind GameStateRepository::class

  single {
    GameListRepositoryInMemory(get(), snapshotConfig = SnapshotConfig())
  } bind GameListRepository::class
}
