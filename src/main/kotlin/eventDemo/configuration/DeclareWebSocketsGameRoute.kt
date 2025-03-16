package eventDemo.configuration

import eventDemo.app.command.GameCommandHandler
import eventDemo.app.command.gameWebSocket
import eventDemo.app.eventListener.PlayerNotificationEventListener
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
