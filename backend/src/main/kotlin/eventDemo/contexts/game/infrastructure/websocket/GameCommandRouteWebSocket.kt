package eventDemo.contexts.game.infrastructure.websocket

import eventDemo.contexts.game.application.channels.GameChannelsSubscriber
import eventDemo.libs.helpers.fromFrameChannel
import eventDemo.libs.helpers.toObjectChannel
import eventDemo.shared.ids.GameId
import eventDemo.sharedKernel.currentUserId
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.uuid.Uuid

@DelicateCoroutinesApi
fun Route.gameWebSocket(channelSubscriber: GameChannelsSubscriber) {
  authenticate {
    webSocket("/games/{id}") {
      channelSubscriber.subscribePlayerToGameChannels(
        gameId = GameId(Uuid.parse(call.parameters["id"]!!)),
        userId = call.currentUserId,
        incomingCommandChannel = toObjectChannel(incoming),
        sendNotificationChannel = fromFrameChannel(outgoing),
      )
    }
  }
}
