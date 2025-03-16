package eventDemo.adapter.interfaceLayer

import eventDemo.business.command.GameCommandHandler
import eventDemo.business.entity.Player
import eventDemo.business.event.eventListener.PlayerNotificationEventListener
import eventDemo.business.notification.Notification
import eventDemo.libs.fromFrameChannel
import eventDemo.libs.toObjectChannel
import io.github.oshai.kotlinlogging.withLoggingContext
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
fun Route.gameWebSocket(
  playerNotificationListener: PlayerNotificationEventListener,
  commandHandler: GameCommandHandler,
) {
  authenticate {
    webSocket("/game") {
      val currentPlayer = call.getPlayer()
      val outgoingFrameChannel: SendChannel<Notification> = fromFrameChannel(outgoing)
      withLoggingContext("currentPlayer" to currentPlayer.toString()) {
        GlobalScope.launch {
          commandHandler.handle(
            currentPlayer,
            toObjectChannel(incoming),
            outgoingFrameChannel,
          )
        }
        playerNotificationListener.startListening({ outgoingFrameChannel.trySendBlocking(it) }, currentPlayer)
      }
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
