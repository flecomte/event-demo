package eventDemo.adapter.presenter.query

import eventDemo.domain.command.GameCommandHandler
import eventDemo.domain.command.command.GameCommand
import eventDemo.domain.entity.GameId
import eventDemo.domain.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.domain.notification.Notification
import eventDemo.libs.fromFrameChannel
import eventDemo.libs.toObjectChannel
import io.github.oshai.kotlinlogging.withLoggingContext
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
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
  val currentPlayer = call.getPlayerCredentials()
  val incomingFrameChannel: ReceiveChannel<GameCommand> = toObjectChannel(incoming)
  val outgoingFrameChannel: SendChannel<Notification> = fromFrameChannel(outgoing)
  withLoggingContext("currentPlayer" to currentPlayer.toString()) {
    val notificationListener =
      playerNotificationListener.startListening(
        currentPlayer,
        gameId,
      ) { outgoingFrameChannel.trySendBlocking(it) }

    // TODO change GlobalScope
    GlobalScope.launch {
      commandHandler.handleIncomingPlayerCommands(
        currentPlayer,
        gameId,
        incomingFrameChannel,
        outgoingFrameChannel,
      )
      notificationListener.close()
    }
  }
}
