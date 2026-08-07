package eventDemo.contexts.game.infrastructure.configuration.injections.infrastructure

import eventDemo.contexts.game.application.channels.GameChannelsSubscriber
import eventDemo.contexts.game.application.command.handlers.GameCommandHandlerDispatcher
import eventDemo.contexts.game.application.notification.CommandSubscriber
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.application.ports.GameEventStore
import eventDemo.contexts.game.application.ports.GameProjectionBus
import eventDemo.contexts.game.infrastructure.persistence.eventBus.GameEventBusInRabbinMQ
import eventDemo.contexts.game.infrastructure.persistence.eventStore.GameEventStoreInPostgresql
import eventDemo.contexts.game.infrastructure.persistence.projections.GameListRepositoryInMemory
import eventDemo.contexts.game.infrastructure.persistence.projections.bus.GameProjectionBusInRabbitMQ
import eventDemo.domain.event.projection.GameListRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Module.configureGameDIInfrastructure() {
  singleOf(::GameEventStoreInPostgresql) bind GameEventStore::class
  singleOf(::GameEventBusInRabbinMQ) bind GameEventBus::class
  singleOf(::GameProjectionBusInRabbitMQ) bind GameProjectionBus::class
  singleOf(::CommandSubscriber)
  singleOf(::GameChannelsSubscriber)
  singleOf(::GameCommandHandlerDispatcher)
  singleOf(::GameListRepositoryInMemory) bind GameListRepository::class
}
