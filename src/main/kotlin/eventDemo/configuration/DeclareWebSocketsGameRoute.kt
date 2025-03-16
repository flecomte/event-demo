package eventDemo.configuration

import eventDemo.adapter.interfaceLayer.gameWebSocket
import eventDemo.business.command.GameCommandHandler
import eventDemo.business.event.eventListener.PlayerNotificationEventListener
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun Application.declareWebSocketsGameRoute(
  playerNotificationListener: PlayerNotificationEventListener,
  commandHandler: GameCommandHandler,
) {
  routing {
    gameWebSocket(playerNotificationListener, commandHandler)
  }
}
