package eventDemo.configuration.injection

import org.koin.dsl.module

fun appKoinModule(config: Configuration) =
  module {
    configureDIBusiness()
    configureDIInfrastructure(config)
    configureDILibs()
    configureDICommandActions()
  }

data class Configuration(
  val redisUrl: String,
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
