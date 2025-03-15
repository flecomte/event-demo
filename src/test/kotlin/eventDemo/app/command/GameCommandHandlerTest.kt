package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.eventListener.PlayerNotificationEventListener
import eventDemo.app.eventListener.ReactionEventListener
import eventDemo.app.notification.CommandSuccessNotification
import eventDemo.app.notification.Notification
import eventDemo.app.notification.WelcomeToTheGameNotification
import eventDemo.configuration.appKoinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.dsl.koinApplication
import kotlin.test.assertIs

@OptIn(DelicateCoroutinesApi::class)
class GameCommandHandlerTest :
  FunSpec({
    test("handle a command should execute the command") {
      koinApplication { modules(appKoinModule) }.koin.apply {
        val commandHandler by inject<GameCommandHandler>()
        val notificationListener by inject<PlayerNotificationEventListener>()
        val gameId = GameId()
        val player = Player("Tesla")
        val channelCommand = Channel<GameCommand>(Channel.BUFFERED)
        val channelNotification = Channel<Notification>(Channel.BUFFERED)
        ReactionEventListener(get(), get(), get()).init()
        notificationListener.startListening(channelNotification, player)

        GlobalScope.launch {
          commandHandler.handle(player, channelCommand, channelNotification)
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
  })
