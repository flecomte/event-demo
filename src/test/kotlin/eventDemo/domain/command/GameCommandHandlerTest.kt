package eventDemo.domain.command

import eventDemo.Tag
import eventDemo.domain.command.command.GameCommand
import eventDemo.domain.command.command.IWantToJoinTheGameCommand
import eventDemo.domain.entity.GameId
import eventDemo.domain.entity.Player
import eventDemo.domain.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.domain.notification.CommandSuccessNotification
import eventDemo.domain.notification.Notification
import eventDemo.domain.notification.WelcomeToTheGameNotification
import eventDemo.testKoinApplicationWithConfig
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
    tags(Tag.Postgresql)

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
            commandHandler.handleIncomingPlayerCommands(
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
