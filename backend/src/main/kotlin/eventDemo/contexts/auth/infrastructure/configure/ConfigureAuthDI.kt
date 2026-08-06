package eventDemo.contexts.auth.infrastructure.configure

import eventDemo.contexts.auth.application.eventStores.UserEventStoreRepository
import eventDemo.contexts.auth.application.eventStores.UserRepository
import eventDemo.contexts.auth.application.ports.UserEventStore
import eventDemo.contexts.auth.application.ports.UserProjectionRepository
import eventDemo.contexts.auth.infrastructure.persistence.eventStore.UserEventStoreInPostgresql
import eventDemo.contexts.auth.infrastructure.persistence.projection.UserProjectionRepositoryInPostgresql
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Module.configureAuthDi() {
  singleOf(::UserEventStoreRepository) bind UserRepository::class
  singleOf(::UserEventStoreInPostgresql) bind UserEventStore::class
  singleOf(::UserProjectionRepositoryInPostgresql) bind UserProjectionRepository::class
}
