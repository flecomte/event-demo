package eventDemo.adapter.interfaceLayer.query

import eventDemo.business.command.GameCommandHandler
import eventDemo.business.command.command.GameCommand
import eventDemo.business.command.command.IWantToJoinTheGameCommand
import eventDemo.business.command.command.IWantToPlayCardCommand
import eventDemo.business.command.command.IamReadyToPlayCommand
import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.event.disableShuffleDeck
import eventDemo.business.event.projection.gameState.GameState
import eventDemo.business.event.projection.gameState.apply
import eventDemo.business.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.business.event.projection.projectionListener.ReactionListener
import eventDemo.business.notification.CommandSuccessNotification
import eventDemo.business.notification.ItsTheTurnOfNotification
import eventDemo.business.notification.Notification
import eventDemo.business.notification.PlayerAsJoinTheGameNotification
import eventDemo.business.notification.PlayerAsPlayACardNotification
import eventDemo.business.notification.PlayerWasReadyNotification
import eventDemo.business.notification.TheGameWasStartedNotification
import eventDemo.business.notification.WelcomeToTheGameNotification
import eventDemo.configuration.injection.appKoinModule
import eventDemo.libs.event.projection.ProjectionSnapshotRepositoryInMemory
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.dsl.koinApplication
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@DelicateCoroutinesApi
class GameSimulationTest :
  FunSpec({
    test("Simulation of a game") {
      withTimeout(2.seconds) {
        disableShuffleDeck()
        val gameId = GameId()
        val player1 = Player(name = "Nikola")
        val player2 = Player(name = "Einstein")
        val channelCommand1 = Channel<GameCommand>(Channel.BUFFERED)
        val channelCommand2 = Channel<GameCommand>(Channel.BUFFERED)
        val channelNotification1 = Channel<Notification>(Channel.BUFFERED)
        val channelNotification2 = Channel<Notification>(Channel.BUFFERED)

        var playedCard1: Card? = null
        var playedCard2: Card? = null

        val player1Job =
          launch {
            IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(gameId, player1)).also { sendCommand ->
              channelCommand1.send(sendCommand)
              channelNotification1.receive().let {
                assertIs<CommandSuccessNotification>(it).commandId shouldBeEqual sendCommand.id
              }
            }

            channelNotification1.receive().let {
              assertIs<WelcomeToTheGameNotification>(it).players shouldBeEqual setOf(player1)
            }
            channelNotification1.receive().let {
              assertIs<PlayerAsJoinTheGameNotification>(it).player shouldBeEqual player2
            }
            IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(gameId, player1)).also { sendCommand ->
              channelCommand1.send(sendCommand)
              channelNotification1.receive().let {
                assertIs<CommandSuccessNotification>(it).commandId shouldBeEqual sendCommand.id
              }
            }
            channelNotification1.receive().let {
              assertIs<PlayerWasReadyNotification>(it).player shouldBeEqual player2
            }
            val player1Hand =
              channelNotification1.receive().let {
                assertIs<TheGameWasStartedNotification>(it).hand shouldHaveSize 7
              }
            playedCard1 = player1Hand.first()
            channelNotification1.receive().let {
              assertIs<ItsTheTurnOfNotification>(it).apply {
                player shouldBeEqual player1
              }
            }

            IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(gameId, player1, player1Hand.first())).also { sendCommand ->
              channelCommand1.send(sendCommand)
              channelNotification1.receive().let {
                assertIs<CommandSuccessNotification>(it).commandId shouldBeEqual sendCommand.id
              }
            }

            channelNotification1.receive().let {
              assertIs<ItsTheTurnOfNotification>(it).apply {
                player shouldBeEqual player2
              }
            }

            channelNotification1.receive().let {
              assertIs<PlayerAsPlayACardNotification>(it).apply {
                player shouldBeEqual player2
                card shouldBeEqual assertNotNull(playedCard2)
              }
            }
          }

        val player2Job =
          launch {
            delay(100)
            IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(gameId, player2)).also { sendCommand ->
              channelCommand2.send(sendCommand)
              channelNotification2.receive().let {
                assertIs<CommandSuccessNotification>(it).commandId shouldBeEqual sendCommand.id
              }
            }

            channelNotification2.receive().let {
              assertIs<WelcomeToTheGameNotification>(it).players shouldBeEqual setOf(player1, player2)
            }
            channelNotification2.receive().let {
              assertIs<PlayerWasReadyNotification>(it).player shouldBeEqual player1
            }

            IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(gameId, player2)).also { sendCommand ->
              channelCommand2.send(sendCommand)
              channelNotification2.receive().let {
                assertIs<CommandSuccessNotification>(it).commandId shouldBeEqual sendCommand.id
              }
            }

            val player2Hand =
              channelNotification2.receive().let {
                assertIs<TheGameWasStartedNotification>(it).hand shouldHaveSize 7
              }
            channelNotification2.receive().let {
              assertIs<ItsTheTurnOfNotification>(it).apply {
                player shouldBeEqual player1
              }
            }
            channelNotification2.receive().let {
              assertIs<PlayerAsPlayACardNotification>(it).apply {
                player shouldBeEqual player1
                card shouldBeEqual assertNotNull(playedCard1)
              }
            }
            playedCard2 = player2Hand.first()

            channelNotification2.receive().let {
              assertIs<ItsTheTurnOfNotification>(it).apply {
                player shouldBeEqual player2
              }
            }

            IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(gameId, player2, player2Hand.first())).also { sendCommand ->
              channelCommand2.send(sendCommand)
              channelNotification2.receive().let {
                assertIs<CommandSuccessNotification>(it).commandId shouldBeEqual sendCommand.id
              }
            }
          }

        koinApplication { modules(appKoinModule) }.koin.apply {
          val commandHandler by inject<GameCommandHandler>()
          val eventStore by inject<GameEventStore>()
          val playerNotificationListener by inject<PlayerNotificationListener>()
          ReactionListener(get(), get()).init()
          playerNotificationListener.startListening(player1, gameId) { channelNotification1.trySendBlocking(it) }
          playerNotificationListener.startListening(player2, gameId) { channelNotification2.trySendBlocking(it) }

          GlobalScope.launch(Dispatchers.IO) {
            commandHandler.handle(player1, gameId, channelCommand1, channelNotification1)
          }
          GlobalScope.launch(Dispatchers.IO) {
            commandHandler.handle(player2, gameId, channelCommand2, channelNotification2)
          }

          joinAll(player1Job, player2Job)

          val state =
            ProjectionSnapshotRepositoryInMemory(
              eventStore = eventStore,
              initialStateBuilder = { aggregateId: GameId -> GameState(aggregateId) },
              applyToProjection = GameState::apply,
            ).getLast(gameId)

          state.aggregateId shouldBeEqual gameId
          assertTrue(state.isStarted)
          state.players shouldBeEqual setOf(player1, player2)
          state.readyPlayers shouldBeEqual setOf(player1, player2)
          state.direction shouldBeEqual GameState.Direction.CLOCKWISE
          assertNotNull(state.lastCardPlayer) shouldBeEqual player2
          assertNotNull(state.cardOnCurrentStack) shouldBeEqual assertNotNull(playedCard2)
        }
      }
    }
  })
