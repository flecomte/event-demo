package eventDemo.configuration.business

import eventDemo.adapter.infrastructureLayer.event.projection.GameListRepositoryInRedis
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInRedis
import eventDemo.business.command.GameCommandHandler
import eventDemo.business.event.projection.projectionListener.ReactionListener
import org.koin.core.Koin

fun Koin.configureGameListener() {
  get<GameCommandHandler>()
    .subscribeToBus(get())

  get<GameStateRepositoryInRedis>()
    .subscribeToBus(get(), get())

  get<GameListRepositoryInRedis>()
    .subscribeToBus(get(), get())

  get<ReactionListener>()
    .subscribeToBus(get())
}
