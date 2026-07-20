package eventDemo.configuration.domain

import eventDemo.adapter.infrastructure.event.projection.GameListRepositoryInRedis
import eventDemo.adapter.infrastructure.event.projection.GameStateRepositoryInRedis
import eventDemo.domain.command.GameCommandHandler
import eventDemo.domain.event.projection.projectionListener.ReactionListener
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
