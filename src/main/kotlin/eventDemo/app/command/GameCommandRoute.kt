package eventDemo.app.command

import eventDemo.app.entity.Player
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import eventDemo.libs.fromFrameChannel
import eventDemo.libs.toObjectChannel
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
fun Route.gameSocket(
    playerNotificationListener: GameEventPlayerNotificationListener,
    commandHandler: GameCommandHandler,
) {
    authenticate {
        webSocket("/game") {
            val currentPlayer = call.getPlayer()
            GlobalScope.launch {
                commandHandler.handle(
                    currentPlayer,
                    toObjectChannel(incoming),
                    fromFrameChannel(outgoing),
                )
            }
            playerNotificationListener.startListening(outgoing, currentPlayer)
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
