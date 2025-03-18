package eventDemo.adapter.interfaceLayer

import eventDemo.business.command.GameCommandHandler
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.business.notification.Notification
import eventDemo.libs.fromFrameChannel
import eventDemo.libs.toObjectChannel
import io.github.oshai.kotlinlogging.withLoggingContext
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch
import java.util.UUID

@DelicateCoroutinesApi
fun Route.gameWebSocket(
  playerNotificationListener: PlayerNotificationListener,
  commandHandler: GameCommandHandler,
) {
  authenticate {
    webSocket("/games/new") {
      runWebSocket(GameId(), commandHandler, playerNotificationListener)
    }

    webSocket("/games/{id}") {
      val gameId = GameId(UUID.fromString(call.parameters["id"]!!))
      runWebSocket(gameId, commandHandler, playerNotificationListener)
    }
  }
}

@DelicateCoroutinesApi
private fun DefaultWebSocketServerSession.runWebSocket(
  gameId: GameId,
  commandHandler: GameCommandHandler,
  playerNotificationListener: PlayerNotificationListener,
) {
  val currentPlayer = call.getPlayer()
  val outgoingFrameChannel: SendChannel<Notification> = fromFrameChannel(outgoing)
  withLoggingContext("currentPlayer" to currentPlayer.toString()) {
    // TODO change GlobalScope
    GlobalScope.launch {
      commandHandler.handle(
        currentPlayer,
        gameId,
        toObjectChannel(incoming),
        outgoingFrameChannel,
      )
    }

    playerNotificationListener.startListening(
      { outgoingFrameChannel.trySendBlocking(it) },
      currentPlayer,
      gameId,
    )
  }
}

private fun ApplicationCall.getPlayer() =
  principal<JWTPrincipal>()!!.run {
    Player(
      id = payload.getClaim("playerid").asString(),
      name = payload.getClaim("username").asString(),
    )
  }
