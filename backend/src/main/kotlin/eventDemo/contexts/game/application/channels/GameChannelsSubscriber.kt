package eventDemo.contexts.game.application.channels

import eventDemo.contexts.game.application.notification.CommandSubscriber
import eventDemo.contexts.game.application.notification.EventToNotificationSubscriber
import eventDemo.shared.game.command.GameCommand
import eventDemo.shared.game.notification.Notification
import eventDemo.shared.ids.GameId
import eventDemo.shared.ids.UserId
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

class GameChannelsSubscriber(
  private val eventToNotificationSubscriber: EventToNotificationSubscriber,
  private val commandSubscriber: CommandSubscriber,
) {
  @DelicateCoroutinesApi
  fun subscribePlayerToGameChannels(
    gameId: GameId,
    userId: UserId,
    incomingCommandChannel: ReceiveChannel<GameCommand>,
    sendNotificationChannel: SendChannel<Notification>,
  ) {
    val sub =
      eventToNotificationSubscriber.subscribeToEventsAndSendNotification(
        gameId = gameId,
        currentUserId = userId,
        outgoingFrameChannel = sendNotificationChannel,
      )

    commandSubscriber
      .subscribe(
        currentUserId = userId,
        incomingFrameChannel = incomingCommandChannel,
      ).invokeOnCompletion { sub.close() }
  }
}
