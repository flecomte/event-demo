package eventDemo.app.query

import eventDemo.app.command.GameCommandHandler
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.command.command.IWantToPlayCardCommand
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.disableShuffleDeck
import eventDemo.app.event.projection.GameState
import eventDemo.app.event.projection.buildStateFromEventStream
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import eventDemo.app.eventListener.GameEventReactionListener
import eventDemo.app.notification.PlayerAsJoinTheGameNotification
import eventDemo.app.notification.PlayerAsPlayACardNotification
import eventDemo.app.notification.PlayerWasReadyNotification
import eventDemo.app.notification.TheGameWasStartedNotification
import eventDemo.app.notification.WelcomeToTheGameNotification
import eventDemo.configuration.appKoinModule
import eventDemo.shared.toFrame
import eventDemo.shared.toNotification
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.websocket.Frame
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.dsl.koinApplication
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DelicateCoroutinesApi
class GameStateTest :
    FunSpec({
        test("Simulation of a game") {
            disableShuffleDeck()
            val id = GameId()
            val player1 = Player(name = "Nikola")
            val player2 = Player(name = "Einstein")
            val channelIn1 = Channel<Frame>()
            val channelIn2 = Channel<Frame>()
            val channelOut1 = Channel<Frame>(Channel.BUFFERED)
            val channelOut2 = Channel<Frame>(Channel.BUFFERED)

            koinApplication { modules(appKoinModule) }.koin.apply {
                val commandHandler by inject<GameCommandHandler>()
                val playerNotificationListener by inject<GameEventPlayerNotificationListener>()
                val eventStream by inject<GameEventStream>()
                GameEventReactionListener(get(), get(), get()).init()
                playerNotificationListener.startListening(channelOut1, player1)
                playerNotificationListener.startListening(channelOut2, player2)

                GlobalScope.launch(Dispatchers.IO) {
                    commandHandler.handle(player1, channelIn1, channelOut1)
                }
                GlobalScope.launch(Dispatchers.IO) {
                    commandHandler.handle(player2, channelIn2, channelOut2)
                }

                channelIn1.send(IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(id, player1)).toFrame())
                delay(50)
                channelIn2.send(IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(id, player2)).toFrame())
                delay(50)
                channelOut1.receive().toNotification().let {
                    assertIs<WelcomeToTheGameNotification>(it).players shouldBeEqual setOf(player1)
                }

                channelOut2.receive().toNotification().let {
                    assertIs<WelcomeToTheGameNotification>(it).players shouldBeEqual setOf(player1, player2)
                }
                channelOut1.receive().toNotification().let {
                    assertIs<PlayerAsJoinTheGameNotification>(it).player shouldBeEqual player2
                }

                channelIn1.send(IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(id, player1)).toFrame())
                delay(50)
                channelIn2.send(IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(id, player2)).toFrame())
                delay(50)

                channelOut1.receive().toNotification().let {
                    assertIs<PlayerWasReadyNotification>(it).player shouldBeEqual player2
                }
                channelOut2.receive().toNotification().let {
                    assertIs<PlayerWasReadyNotification>(it).player shouldBeEqual player1
                }

                val player1Hand =
                    channelOut1.receive().toNotification().let {
                        assertIs<TheGameWasStartedNotification>(it).hand shouldHaveSize 7
                    }
                val player2Hand =
                    channelOut2.receive().toNotification().let {
                        assertIs<TheGameWasStartedNotification>(it).hand shouldHaveSize 7
                    }

                channelIn1.send(IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(id, player1, player1Hand.first())).toFrame())
                delay(50)
                channelOut2.receive().toNotification().let {
                    assertIs<PlayerAsPlayACardNotification>(it).player shouldBeEqual player1
                    assertIs<PlayerAsPlayACardNotification>(it).card shouldBeEqual player1Hand.first()
                }

                channelIn2.send(IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(id, player2, player2Hand.first())).toFrame())
                delay(50)
                channelOut1.receive().toNotification().let {
                    assertIs<PlayerAsPlayACardNotification>(it).player shouldBeEqual player2
                    assertIs<PlayerAsPlayACardNotification>(it).card shouldBeEqual player2Hand.first()
                }

                val state = id.buildStateFromEventStream(eventStream)

                state.gameId shouldBeEqual id
                assertTrue(state.isStarted)
                state.players shouldBeEqual setOf(player1, player2)
                state.readyPlayers shouldBeEqual setOf(player1, player2)
                state.direction shouldBeEqual GameState.Direction.CLOCKWISE
                assertNotNull(state.lastCard) shouldBeEqual GameState.LastCard(player2Hand.first(), player2)
            }
        }
    })
