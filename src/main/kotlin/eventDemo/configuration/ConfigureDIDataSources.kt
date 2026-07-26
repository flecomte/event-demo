package eventDemo.configuration

import com.rabbitmq.client.ConnectionFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeCallback
import org.koin.dsl.bind
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.UnifiedJedis
import javax.sql.DataSource

fun Module.configureDIDataSource(config: Configuration) {
  // PostgreSQL (for EventStore)
  single {
    hikariDataSource(config)
      .apply {
        registerCallback(
          object : ScopeCallback {
            override fun onScopeClose(scope: Scope) {
              close()
            }
          },
        )
      }
  } bind DataSource::class

  // Redis (for Projections)
  single {
    JedisPooled(config.redisUrl)
  } bind UnifiedJedis::class

  // RabbitMQ (for EventBus)
  factory {
    ConnectionFactory().apply {
      host = config.rabbitmq.url
      port = config.rabbitmq.port
      username = config.rabbitmq.username
      password = config.rabbitmq.password
    }
  }
}

private fun hikariDataSource(config: Configuration): HikariDataSource =
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
