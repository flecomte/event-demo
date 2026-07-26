package eventDemo.configuration

import io.ktor.server.config.ApplicationConfig

data class Configuration(
  val redisUrl: String,
  val jwtSecret: String,
  val postgresql: Postgresql,
  val rabbitmq: RabbitMQ,
) {
  data class Postgresql(
    val url: String,
    val username: String,
    val password: String,
  )

  data class RabbitMQ(
    val url: String,
    val port: Int,
    val username: String,
    val password: String,
  )
}

val ApplicationConfig.configuration
  get() =
    Configuration(
      redisUrl = getProperty("redis.url"),
      jwtSecret = getProperty("jwt.secret"),
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
