package eventDemo.contexts.game.application.channels

import eventDemo.contexts.game.application.command.models.GameCommand
import eventDemo.contexts.game.application.notification.CommandSubscriber
import eventDemo.contexts.game.application.notification.EventToNotificationSubscriber
import eventDemo.contexts.game.application.notification.models.Notification
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.sharedKernel.UserId
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
