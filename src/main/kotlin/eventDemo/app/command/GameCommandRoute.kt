package eventDemo.app.command

import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventStream
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket

fun Route.gameSocket(
    eventStream: GameEventStream,
    eventBus: GameEventBus,
) {
    authenticate {
        webSocket("/game") {
            GameCommandHandler(eventStream, incoming, outgoing).init(call.getPlayer())
            GameEventPlayerNotificationListener(eventBus, outgoing).init()
        }
    }
}

private fun ApplicationCall.getPlayer() =
    principal<JWTPrincipal>()!!.run {
        Player(
            id = payload.getClaim("playerid").asString(),
            name = payload.getClaim("username").asString(),
        )
    }
