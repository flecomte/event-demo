package eventDemo.configuration.business

import eventDemo.adapter.infrastructureLayer.event.projection.GameListRepositoryInMemory
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInMemory
import eventDemo.business.command.GameCommandHandler
import eventDemo.business.event.projection.projectionListener.ReactionListener
import org.koin.core.Koin

fun Koin.configureGameListener() {
  get<ReactionListener>()
    .subscribeToBus(get())

  get<GameStateRepositoryInMemory>()
    .subscribeToBus(get(), get())

  get<GameListRepositoryInMemory>()
    .subscribeToBus(get(), get())

  get<GameCommandHandler>()
    .subscribeToBus(get())
}
