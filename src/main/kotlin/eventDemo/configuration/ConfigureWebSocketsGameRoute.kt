package eventDemo.configuration

import eventDemo.app.command.GameCommandHandler
import eventDemo.app.command.gameSocket
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun Application.declareWebSocketsGameRoute(
    playerNotificationListener: GameEventPlayerNotificationListener,
    commandHandler: GameCommandHandler,
) {
    routing {
        gameSocket(playerNotificationListener, commandHandler)
    }
}
