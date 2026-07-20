package eventDemo.configuration.route

import eventDemo.adapter.presenter.query.gameWebSocket
import eventDemo.domain.command.GameCommandHandler
import eventDemo.domain.event.projection.projectionListener.PlayerNotificationListener
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun Application.declareWebSocketsGameRoute(
  playerNotificationListener: PlayerNotificationListener,
  commandHandler: GameCommandHandler,
) {
  routing {
    gameWebSocket(playerNotificationListener, commandHandler)
  }
}
