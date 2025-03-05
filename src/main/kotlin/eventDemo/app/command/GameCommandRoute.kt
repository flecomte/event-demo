package eventDemo.app.command

import eventDemo.app.entity.Player
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket

fun Route.gameSocket(
    playerNotificationListener: GameEventPlayerNotificationListener,
    commandHandler: GameCommandHandler,
) {
    authenticate {
        webSocket("/game") {
            commandHandler.handle(call.getPlayer(), incoming, outgoing)
            playerNotificationListener.startListening(outgoing)
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
