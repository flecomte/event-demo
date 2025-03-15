package eventDemo.configuration

import eventDemo.app.command.GameCommandActionRunner
import eventDemo.app.command.GameCommandHandler
import eventDemo.app.command.command.GameCommand
import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.GameEventStore
import eventDemo.app.event.projection.GameStateRepository
import eventDemo.app.event.projection.SnapshotConfig
import eventDemo.app.eventListener.PlayerNotificationEventListener
import eventDemo.libs.command.CommandRunnerController
import eventDemo.libs.command.CommandStreamChannel
import eventDemo.libs.event.EventBusInMemory
import eventDemo.libs.event.EventStoreInMemory
import eventDemo.libs.event.VersionBuilder
import eventDemo.libs.event.VersionBuilderLocal
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
  install(Koin) {
    slf4jLogger()
    modules(appKoinModule)
  }
}

val appKoinModule =
  module {
    single {
      GameEventBus(EventBusInMemory())
    }
    single {
      GameEventStore(EventStoreInMemory())
    }
    single {
      GameStateRepository(get(), get(), snapshotConfig = SnapshotConfig())
    }
    single {
      CommandStreamChannel<GameCommand>(get())
    }
    single {
      CommandRunnerController<GameCommand>()
    }
    single {
      GameCommandHandler(get(), get(), get(), get())
    }

    singleOf(::VersionBuilderLocal) bind VersionBuilder::class
    singleOf(::GameEventHandler)
    singleOf(::GameCommandActionRunner)
    singleOf(::PlayerNotificationEventListener)

    // Actions
    configureActions()
  }
