package eventDemo.configuration

import eventDemo.adapter.infrastructureLayer.event.GameEventBusInMemory
import eventDemo.adapter.infrastructureLayer.event.GameEventStoreInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInMemory
import eventDemo.business.command.GameCommandActionRunner
import eventDemo.business.command.GameCommandHandler
import eventDemo.business.command.command.GameCommand
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.eventListener.PlayerNotificationEventListener
import eventDemo.business.event.projection.GameStateRepository
import eventDemo.libs.command.CommandRunnerController
import eventDemo.libs.command.CommandStreamChannel
import eventDemo.libs.event.VersionBuilder
import eventDemo.libs.event.VersionBuilderLocal
import eventDemo.libs.event.projection.SnapshotConfig
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
      GameEventBusInMemory()
    } bind GameEventBus::class

    single {
      GameEventStoreInMemory()
    } bind GameEventStore::class

    single {
      GameStateRepositoryInMemory(get(), get(), snapshotConfig = SnapshotConfig())
    } bind GameStateRepository::class

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
