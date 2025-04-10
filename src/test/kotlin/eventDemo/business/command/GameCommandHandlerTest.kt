package eventDemo.business.command

import eventDemo.business.command.command.GameCommand
import eventDemo.business.command.command.IWantToJoinTheGameCommand
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.business.notification.CommandSuccessNotification
import eventDemo.business.notification.Notification
import eventDemo.business.notification.WelcomeToTheGameNotification
import eventDemo.testKoinApplicationWithConfig
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class GameCommandHandlerTest :
  FunSpec({
    tags(NamedTag("postgresql"))

    test("handle a command should execute the command") {
      withTimeout(5.seconds) {
        testKoinApplicationWithConfig {
          val commandHandler = get<GameCommandHandler>()
          val notificationListener = get<PlayerNotificationListener>()
          val gameId = GameId()
          val player = Player("Tesla")
          val channelCommand = Channel<GameCommand>(Channel.BUFFERED)
          val channelNotification = Channel<Notification>(Channel.BUFFERED)
          notificationListener.startListening(
            player,
            gameId,
          ) { channelNotification.trySendBlocking(it) }

          GlobalScope.launch {
            commandHandler.handle(
              player,
              gameId,
              channelCommand,
              channelNotification,
            )
          }

          IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(gameId, player)).also { sendCommand ->
            channelCommand.send(sendCommand)
            channelNotification.receive().let {
              assertIs<CommandSuccessNotification>(it).commandId shouldBeEqual sendCommand.id
            }
          }
          assertIs<WelcomeToTheGameNotification>(channelNotification.receive()).let {
            it.players shouldContain player
          }
        }
      }
    }
  })
