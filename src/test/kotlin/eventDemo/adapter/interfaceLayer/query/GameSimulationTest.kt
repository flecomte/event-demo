package eventDemo.adapter.interfaceLayer.query

import eventDemo.Tag
import eventDemo.adapter.infrastructureLayer.event.projection.GameStateRepositoryInMemory
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
import eventDemo.business.event.projection.GameState
import eventDemo.business.event.projection.projectionListener.PlayerNotificationListener
import eventDemo.business.notification.CommandSuccessNotification
import eventDemo.business.notification.ItsTheTurnOfNotification
import eventDemo.business.notification.Notification
import eventDemo.business.notification.PlayerAsJoinTheGameNotification
import eventDemo.business.notification.PlayerAsPlayACardNotification
import eventDemo.business.notification.PlayerWasReadyNotification
import eventDemo.business.notification.TheGameWasStartedNotification
import eventDemo.business.notification.WelcomeToTheGameNotification
import eventDemo.testKoinApplicationWithConfig
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.until
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@DelicateCoroutinesApi
class GameSimulationTest :
  FunSpec({
    tags(Tag.Postgresql)

    test("Simulation of a game") {
      withTimeout(10.seconds) {
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

        var player1HasJoin = false

        testKoinApplicationWithConfig {
          val commandHandler by inject<GameCommandHandler>()
          val eventStore by inject<GameEventStore>()
          val playerNotificationListener by inject<PlayerNotificationListener>()

          // Run command handler
          // In the normal process, these handlers is invoque players connect to the websocket
          run {
            GlobalScope.launch(Dispatchers.IO) {
              commandHandler.handle(player1, gameId, channelCommand1, channelNotification1)
            }
            GlobalScope.launch(Dispatchers.IO) {
              commandHandler.handle(player2, gameId, channelCommand2, channelNotification2)
            }
          }

          // Consume etch notification of players, and put theses in list.
          // is used later to control when other players can be executing the next action
          val player1Notifications = mutableListOf<Notification>()
          val player2Notifications = mutableListOf<Notification>()
          run {
            GlobalScope.launch {
              for (notification in channelNotification1) {
                player1Notifications.add(notification)
              }
            }

            GlobalScope.launch {
              for (notification in channelNotification2) {
                player2Notifications.add(notification)
              }
            }
          }

          // The player 1 actions
          val player1Job =
            launch {
              playerNotificationListener.startListening(player1, gameId) {
                channelNotification1.trySendBlocking(it)
              }
              IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(gameId, player1)).also { sendCommand ->
                channelCommand1.send(sendCommand)
                player1Notifications.waitNotification<CommandSuccessNotification> { commandId == sendCommand.id }
              }

              player1HasJoin = true

              player1Notifications.waitNotification<WelcomeToTheGameNotification> { players == setOf(player1) }
              player1Notifications.waitNotification<PlayerAsJoinTheGameNotification> { player == player2 }

              IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(gameId, player1)).also { sendCommand ->
                channelCommand1.send(sendCommand)
                player1Notifications.waitNotification<CommandSuccessNotification> { commandId == sendCommand.id }
              }
              player1Notifications.waitNotification<PlayerWasReadyNotification> { player == player2 }
              val player1Hand = player1Notifications.waitNotification<TheGameWasStartedNotification> { hand.size == 7 }.hand

              playedCard1 = player1Hand.first()
              player1Notifications.waitNotification<ItsTheTurnOfNotification> { player == player1 }

              IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(gameId, player1, player1Hand.first())).also { sendCommand ->
                channelCommand1.send(sendCommand)
                player1Notifications.waitNotification<CommandSuccessNotification> { commandId == sendCommand.id }
              }

              player1Notifications.waitNotification<ItsTheTurnOfNotification> { player == player2 }

              player1Notifications.waitNotification<PlayerAsPlayACardNotification> {
                player == player2 && card == playedCard2
              }
            }

          // The player 2 actions
          val player2Job =
            launch {
              // wait the player 1 has join the game
              until(1.seconds) { player1HasJoin }

              playerNotificationListener.startListening(player2, gameId) {
                channelNotification2.trySendBlocking(it)
              }

              IWantToJoinTheGameCommand(IWantToJoinTheGameCommand.Payload(gameId, player2)).also { sendCommand ->
                channelCommand2.send(sendCommand)
                player2Notifications.waitNotification<CommandSuccessNotification> { commandId == sendCommand.id }
              }

              player2Notifications.waitNotification<WelcomeToTheGameNotification> { players == setOf(player1, player2) }
              player2Notifications.waitNotification<PlayerWasReadyNotification> { player == player1 }

              IamReadyToPlayCommand(IamReadyToPlayCommand.Payload(gameId, player2)).also { sendCommand ->
                channelCommand2.send(sendCommand)
                player2Notifications.waitNotification<CommandSuccessNotification> { commandId == sendCommand.id }
              }

              val player2Hand =
                player2Notifications.waitNotification<TheGameWasStartedNotification> { hand.size == 7 }.hand

              player2Notifications.waitNotification<ItsTheTurnOfNotification> { player == player1 }
              player2Notifications.waitNotification<PlayerAsPlayACardNotification> {
                player == player1 && card == playedCard1
              }
              playedCard2 = player2Hand.first()

              player2Notifications.waitNotification<ItsTheTurnOfNotification> { player == player2 }

              IWantToPlayCardCommand(IWantToPlayCardCommand.Payload(gameId, player2, player2Hand.first())).also { sendCommand ->
                channelCommand2.send(sendCommand)
                player2Notifications.waitNotification<CommandSuccessNotification> { commandId == sendCommand.id }
              }
            }

          // Wait the end of the game
          joinAll(player1Job, player2Job)

          // Build the last state from the event store
          val state = GameStateRepositoryInMemory(eventStore = eventStore).getLast(gameId)

          // Check if the state is correct
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

private suspend inline fun <reified T : Notification> MutableList<Notification>.waitNotification(crossinline block: T.() -> Boolean): T =
  eventually(1.seconds) {
    filterIsInstance<T>().first { block(it) }
  }
