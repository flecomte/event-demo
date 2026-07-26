package eventDemo.configuration

import eventDemo.contexts.auth.infrastructure.configure.configureAuthDi
import eventDemo.contexts.game.infrastructure.configuration.injections.application.configureGameDIApplication
import eventDemo.contexts.game.infrastructure.configuration.injections.infrastructure.configureGameDIInfrastructure
import org.koin.dsl.module

fun appKoinModule(config: Configuration) =
  module {
    configureDIDataSource(config)
    configureAuthDi()
    configureGameDIInfrastructure()
    configureGameDIApplication()
  }
