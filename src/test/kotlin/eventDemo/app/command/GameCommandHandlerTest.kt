package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import eventDemo.app.eventListener.GameEventReactionListener
import eventDemo.app.notification.Notification
import eventDemo.app.notification.WelcomeToTheGameNotification
import eventDemo.configuration.appKoinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.dsl.koinApplication
import kotlin.test.assertIs

class GameCommandHandlerTest :
    FunSpec({
        test("handle a command should execute the command") {
            koinApplication { modules(appKoinModule) }.koin.apply {
                val commandHandler by inject<GameCommandHandler>()
                val notificationListener by inject<GameEventPlayerNotificationListener>()
                val gameId = GameId()
                val player = Player("Tesla")
                val channelCommand = Channel<GameCommand>(Channel.BUFFERED)
                val channelNotification = Channel<Notification>(Channel.BUFFERED)
                GameEventReactionListener(get(), get(), get()).init()
                notificationListener.startListening(channelNotification, player)

                GlobalScope.launch {
                    commandHandler.handle(player, channelCommand, channelNotification)
                }

                channelCommand.send(IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(gameId, player)))
                assertIs<WelcomeToTheGameNotification>(channelNotification.receive()).let {
                    it.players shouldContain player
                }
            }
        }
    })
