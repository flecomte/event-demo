package eventDemo.configuration

import eventDemo.app.command.gameSocket
import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventStream
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.declareWebSocketsGameRoute(
    eventStream: GameEventStream,
    eventBus: GameEventBus,
) {
    routing {
        gameSocket(eventStream, eventBus)
    }
}
