package eventDemo.configuration.ktor

import eventDemo.configuration.injection.Configuration
import eventDemo.configuration.injection.appKoinModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
  install(Koin) {
    slf4jLogger()

    modules(
      appKoinModule(
        environment.config.configuration(),
      ),
    )
  }
}

fun ApplicationConfig.configuration() =
  Configuration(
    redisUrl = getProperty("redis.url"),
    postgresql =
      Configuration.Postgresql(
        url = getProperty("postgresql.url"),
        username = getProperty("postgresql.username"),
        password = getProperty("postgresql.password"),
      ),
    rabbitmq =
      Configuration.RabbitMQ(
        url = getProperty("rabbitmq.url"),
        port = getProperty("rabbitmq.port").toInt(),
        username = getProperty("rabbitmq.username"),
        password = getProperty("rabbitmq.password"),
      ),
  )

private fun ApplicationConfig.getProperty(path: String): String =
  propertyOrNull(path)?.getString() ?: error("You must set the $path")
