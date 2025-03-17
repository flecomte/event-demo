package eventDemo.configuration.injection

import eventDemo.adapter.infrastructureLayer.event.GameEventBusInMemory
import eventDemo.adapter.infrastructureLayer.event.GameEventStoreInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameListRepositoryInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInMemory
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.projection.gameList.GameListRepository
import eventDemo.business.event.projection.gameState.GameStateRepository
import eventDemo.libs.event.projection.SnapshotConfig
import org.koin.core.module.Module
import org.koin.dsl.bind

fun Module.configureDIInfrastructure() {
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
    GameListRepositoryInMemory(get(), get(), snapshotConfig = SnapshotConfig())
  } bind GameListRepository::class
}
