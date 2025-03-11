package eventDemo.app.query

import eventDemo.app.command.GameCommandHandler
import eventDemo.app.command.command.IWantToJoinTheGameCommand
import eventDemo.app.command.command.IWantToPlayCardCommand
import eventDemo.app.command.command.IamReadyToPlayCommand
import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.disableShuffleDeck
import eventDemo.app.event.projection.GameState
import eventDemo.app.event.projection.buildStateFromEventStream
import eventDemo.app.eventListener.GameEventPlayerNotificationListener
import eventDemo.app.eventListener.GameEventReactionListener
import eventDemo.app.notification.ItsTheTurnOfNotification
import eventDemo.app.notification.PlayerAsJoinTheGameNotification
import eventDemo.app.notification.PlayerAsPlayACardNotification
import eventDemo.app.notification.PlayerWasReadyNotification
import eventDemo.app.notification.TheGameWasStartedNotification
import eventDemo.app.notification.WelcomeToTheGameNotification
import eventDemo.configuration.appKoinModule
import eventDemo.libs.fromFrameChannel
import eventDemo.libs.toObjectChannel
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
import kotlinx.coroutines.joinAll
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
            val channelCommand1 = Channel<Frame>(Channel.BUFFERED)
            val channelCommand2 = Channel<Frame>(Channel.BUFFERED)
            val channelNotification1 = Channel<Frame>(Channel.BUFFERED)
            val channelNotification2 = Channel<Frame>(Channel.BUFFERED)

            var playedCard1: Card? = null
            var playedCard2: Card? = null

            val player1Job =
                launch {
                    channelCommand1.send(IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(id, player1)).toFrame())
                    channelNotification1.receive().toNotification().let {
                        assertIs<WelcomeToTheGameNotification>(it).players shouldBeEqual setOf(player1)
                    }
                    channelNotification1.receive().toNotification().let {
                        assertIs<PlayerAsJoinTheGameNotification>(it).player shouldBeEqual player2
                    }
                    channelCommand1.send(IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(id, player1)).toFrame())
                    channelNotification1.receive().toNotification().let {
                        assertIs<PlayerWasReadyNotification>(it).player shouldBeEqual player2
                    }
                    val player1Hand =
                        channelNotification1.receive().toNotification().let {
                            assertIs<TheGameWasStartedNotification>(it).hand shouldHaveSize 7
                        }
                    playedCard1 = player1Hand.first()
                    channelNotification1.receive().toNotification().let {
                        assertIs<ItsTheTurnOfNotification>(it).apply {
                            player shouldBeEqual player1
                        }
                    }
                    channelCommand1.send(IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(id, player1, player1Hand.first())).toFrame())

                    channelNotification1.receive().toNotification().let {
                        assertIs<ItsTheTurnOfNotification>(it).apply {
                            player shouldBeEqual player2
                        }
                    }

                    channelNotification1.receive().toNotification().let {
                        assertIs<PlayerAsPlayACardNotification>(it).apply {
                            player shouldBeEqual player2
                            card shouldBeEqual assertNotNull(playedCard2)
                        }
                    }
                }

            val player2Job =
                launch {
                    delay(100)
                    channelCommand2.send(IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(id, player2)).toFrame())
                    channelNotification2.receive().toNotification().let {
                        assertIs<WelcomeToTheGameNotification>(it).players shouldBeEqual setOf(player1, player2)
                    }
                    channelNotification2.receive().toNotification().let {
                        assertIs<PlayerWasReadyNotification>(it).player shouldBeEqual player1
                    }
                    channelCommand2.send(IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(id, player2)).toFrame())
                    val player2Hand =
                        channelNotification2.receive().toNotification().let {
                            assertIs<TheGameWasStartedNotification>(it).hand shouldHaveSize 7
                        }
                    channelNotification2.receive().toNotification().let {
                        assertIs<ItsTheTurnOfNotification>(it).apply {
                            player shouldBeEqual player1
                        }
                    }
                    channelNotification2.receive().toNotification().let {
                        assertIs<PlayerAsPlayACardNotification>(it).apply {
                            player shouldBeEqual player1
                            card shouldBeEqual assertNotNull(playedCard1)
                        }
                    }
                    playedCard2 = player2Hand.first()

                    channelNotification2.receive().toNotification().let {
                        assertIs<ItsTheTurnOfNotification>(it).apply {
                            player shouldBeEqual player2
                        }
                    }
                    channelCommand2.send(IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(id, player2, player2Hand.first())).toFrame())
                }

            koinApplication { modules(appKoinModule) }.koin.apply {
                val commandHandler by inject<GameCommandHandler>()
                val eventStream by inject<GameEventStream>()
                val playerNotificationListener by inject<GameEventPlayerNotificationListener>()
                GameEventReactionListener(get(), get(), get()).init()
                playerNotificationListener.startListening(channelNotification1, player1)
                playerNotificationListener.startListening(channelNotification2, player2)

                GlobalScope.launch(Dispatchers.IO) {
                    commandHandler.handle(player1, toObjectChannel(channelCommand1), fromFrameChannel(channelNotification1))
                }
                GlobalScope.launch(Dispatchers.IO) {
                    commandHandler.handle(player2, toObjectChannel(channelCommand2), fromFrameChannel(channelNotification2))
                }

                joinAll(player1Job, player2Job)

                val state = id.buildStateFromEventStream(eventStream)

                state.gameId shouldBeEqual id
                assertTrue(state.isStarted)
                state.players shouldBeEqual setOf(player1, player2)
                state.readyPlayers shouldBeEqual setOf(player1, player2)
                state.direction shouldBeEqual GameState.Direction.CLOCKWISE
                assertNotNull(state.cardOnCurrentStack) shouldBeEqual GameState.LastCard(assertNotNull(playedCard2), player2)
            }
        }
    })
