package eventDemo.contexts.game.infrastructure.configuration.ktor

import eventDemo.contexts.game.infrastructure.websocket.gameWebSocket
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi
import org.koin.ktor.ext.get as getDi

@OptIn(DelicateCoroutinesApi::class)
fun Application.declareWebSocketsRoute() {
  routing {
    gameWebSocket(
      getDi(),
    )
  }
}
