package eventDemo.app.command

import eventDemo.business.command.GameCommandHandler
import eventDemo.business.command.command.GameCommand
import eventDemo.business.command.command.IWantToJoinTheGameCommand
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.business.event.projection.projectionListener.ReactionListener
import eventDemo.business.notification.CommandSuccessNotification
import eventDemo.business.notification.Notification
import eventDemo.business.notification.WelcomeToTheGameNotification
import eventDemo.configuration.injection.appKoinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch
import org.koin.dsl.koinApplication
import kotlin.test.assertIs

@OptIn(DelicateCoroutinesApi::class)
class GameCommandHandlerTest :
  FunSpec({
    test("handle a command should execute the command") {
      koinApplication { modules(appKoinModule) }.koin.apply {
        val commandHandler by inject<GameCommandHandler>()
        val notificationListener by inject<PlayerNotificationListener>()
        val gameId = GameId()
        val player = Player("Tesla")
        val channelCommand = Channel<GameCommand>(Channel.BUFFERED)
        val channelNotification = Channel<Notification>(Channel.BUFFERED)
        ReactionListener(get(), get()).init()
        notificationListener.startListening(
          { channelNotification.trySendBlocking(it) },
          player,
          gameId,
        )

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
  })
