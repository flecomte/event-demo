package eventDemo.contexts.game.application.notification

import eventDemo.contexts.auth.application.eventStores.UserEventStoreRepository
import eventDemo.contexts.auth.application.eventStores.UserRepository
import eventDemo.contexts.auth.infrastructure.persistence.eventStore.UserEventStoreInMemory
import eventDemo.contexts.game.application.command.handlers.GameCommandHandlerDispatcher
import eventDemo.contexts.game.application.command.handlers.JoinTheGameHandler
import eventDemo.contexts.game.application.command.handlers.PlayCardHandler
import eventDemo.contexts.game.application.command.handlers.ReadyToPlayHandler
import eventDemo.contexts.game.application.command.handlers.TakeCartFromDrawPileHandler
import eventDemo.contexts.game.application.command.models.JoinTheGameCommand
import eventDemo.contexts.game.application.eventStores.GameEventStoreRepository
import eventDemo.contexts.game.application.notification.models.Notification
import eventDemo.contexts.game.application.notification.models.WelcomeToTheGameNotification
import eventDemo.contexts.game.infrastructure.persistence.eventBus.GameEventBusInMemory
import eventDemo.contexts.game.infrastructure.persistence.eventStore.GameEventStoreInMemory
import eventDemo.sharedKernel.UserId
import eventDemo.testHelpers.createNewUser
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.equals.shouldEqual
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.junit.jupiter.api.assertInstanceOf
import kotlin.time.Duration.Companion.seconds

class EventToNotificationSubscriberTest :
  FunSpec({

    test("When event when send to the bus, a notification should be published") {
      val bus = GameEventBusInMemory()
      val gameRepository = GameEventStoreRepository(GameEventStoreInMemory())
      val userRepository = UserEventStoreRepository(UserEventStoreInMemory())
      val subscriber =
        EventToNotificationSubscriber(
          bus,
          gameRepository,
        )

      val commentDispatcher =
        GameCommandHandlerDispatcher(
          PlayCardHandler(gameRepository, bus),
          ReadyToPlayHandler(gameRepository, bus),
          JoinTheGameHandler(gameRepository, bus, userRepository),
          TakeCartFromDrawPileHandler(gameRepository, bus),
        )
      val notificationChannel = Channel<Notification>(Channel.BUFFERED)

      val game = gameRepository.create()

      val user1 = createNewUser("user1")
      val user2 = createNewUser("user2")
      userRepository.run {
        save(user1)
        save(user2)
      }

      val player1Notifications = mutableListOf<Notification>()
      GlobalScope.launch {
        for (notification in notificationChannel) {
          player1Notifications.add(notification)
        }
      }

      commentDispatcher.dispatch(JoinTheGameCommand(user1.id, JoinTheGameCommand.Payload(game.aggregateId)))
      subscriber
        .subscribeToEventsAndSendNotification(
          game.aggregateId,
          user2.id,
          notificationChannel,
        ).use {
          commentDispatcher.dispatch(JoinTheGameCommand(user2.id, JoinTheGameCommand.Payload(game.aggregateId)))
        }

      eventually(duration = 1.seconds) {
        player1Notifications.size shouldEqual 1
      }
      player1Notifications.first().let { notification ->
        assertInstanceOf<WelcomeToTheGameNotification>(notification)
        notification.players.map { it.userId } shouldContain user2.id
      }
    }
  })
