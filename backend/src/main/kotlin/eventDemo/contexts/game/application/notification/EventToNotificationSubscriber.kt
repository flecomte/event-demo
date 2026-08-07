package eventDemo.contexts.game.application.notification

import eventDemo.contexts.game.application.command.handlers.GameCommandHandlerDispatcher
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.logging.LoggingContextKeys.Command
import eventDemo.contexts.game.application.logging.LoggingContextKeys.CurrentUserId
import eventDemo.contexts.game.application.logging.LoggingContextKeys.Event
import eventDemo.contexts.game.application.logging.LoggingContextKeys.Game
import eventDemo.contexts.game.application.logging.LoggingContextKeys.Notification
import eventDemo.contexts.game.application.logging.withLoggingContext
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.libs.bus.Bus
import eventDemo.libs.command.CommandUnicityChecker
import eventDemo.shared.game.command.GameCommand
import eventDemo.shared.game.notification.Notification
import eventDemo.shared.ids.GameId
import eventDemo.shared.ids.UserId
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch

class EventToNotificationSubscriber(
  private val gameEventBus: GameEventBus,
  private val gameRepository: GameRepository,
) {
  fun subscribeToEventsAndSendNotification(
    gameId: GameId,
    currentUserId: UserId,
    outgoingFrameChannel: SendChannel<Notification>,
  ): Bus.Subscription =
    withLoggingContext(CurrentUserId to currentUserId) {
      gameEventBus.subscribe { event ->
        val game = gameRepository.get(gameId) ?: error("Game not found")
        withLoggingContext(Event to event, Game to game) {
          event
            .toNotification(
              game = game,
              currentUserId = currentUserId,
            ).forEach { notification ->
              withLoggingContext(Notification to notification) {
                outgoingFrameChannel.trySendBlocking(notification)
              }
            }
        }
      }
    }
}

class CommandSubscriber(
  private val gameCommandHandlerDispatcher: GameCommandHandlerDispatcher,
) {
  private val controller = CommandUnicityChecker<GameCommand>()

  @DelicateCoroutinesApi
  fun subscribe(
    currentUserId: UserId,
    incomingFrameChannel: ReceiveChannel<GameCommand>,
  ): Job =
    GlobalScope.launch {
      for (command in incomingFrameChannel) {
        withLoggingContext(CurrentUserId to currentUserId, Command to command) {
          controller.runOnlyOnce(command) {
            gameCommandHandlerDispatcher.dispatch(command)
          }
        }
      }
    }
}
