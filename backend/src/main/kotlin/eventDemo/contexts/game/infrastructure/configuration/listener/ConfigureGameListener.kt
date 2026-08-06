package eventDemo.contexts.game.infrastructure.configuration.listener

import eventDemo.domain.event.projection.GameListRepository
import org.koin.core.Koin

fun Koin.configureProjectionListener() {
  get<GameListRepository>()
    .subscribeToBus()
}
