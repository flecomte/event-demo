package eventDemo.plugins

import eventDemo.app.actions.GameCommandHandler
import eventDemo.app.actions.GameEventPlayerNotificationSubscriber
import eventDemo.shared.entity.Player
import eventDemo.shared.event.GameEventBus
import eventDemo.shared.event.GameEventStream
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}

fun Application.configureWebSocketsGameRoute(
    eventStream: GameEventStream,
    eventBus: GameEventBus,
) {
    routing {
        authenticate {
            webSocket("/game") {
                GameCommandHandler(eventStream, incoming, outgoing).init(call.getPlayer())
                GameEventPlayerNotificationSubscriber(eventBus, outgoing).init()
            }
        }
    }
}

fun ApplicationCall.getPlayer() =
    principal<JWTPrincipal>()!!.run {
        Player(
            id = payload.getClaim("playerid").asString(),
            name = payload.getClaim("username").asString(),
        )
    }
