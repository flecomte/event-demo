package eventDemo.configuration.injection

import org.koin.dsl.module

fun appKoinModule(config: Configuration) =
  module {
    configureDIBusiness()
    configureDIInfrastructure(config.redisUrl)
    configureDILibs()
    configureDICommandActions()
  }

data class Configuration(
  val redisUrl: String,
)
