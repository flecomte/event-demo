package eventDemo.contexts.game.infrastructure.websocket

import eventDemo.contexts.game.application.channels.GameChannelsSubscriber
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.libs.helpers.fromFrameChannel
import eventDemo.libs.helpers.toObjectChannel
import eventDemo.sharedKernel.currentUserId
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.UUID

@DelicateCoroutinesApi
fun Route.gameWebSocket(channelSubscriber: GameChannelsSubscriber) {
  authenticate {
    webSocket("/games/{id}") {
      channelSubscriber.subscribePlayerToGameChannels(
        gameId = GameId(UUID.fromString(call.parameters["id"]!!)),
        userId = call.currentUserId,
        incomingCommandChannel = toObjectChannel(incoming),
        sendNotificationChannel = fromFrameChannel(outgoing),
      )
    }
  }
}
