package eventDemo.configuration

import eventDemo.app.command.GameCommandHandler
import eventDemo.app.command.gameSocket
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.declareWebSocketsGameRoute(
    playerNotificationListener: GameEventPlayerNotificationListener,
    commandHandler: GameCommandHandler,
) {
    routing {
        gameSocket(playerNotificationListener, commandHandler)
    }
}
