package eventDemo.contexts.game.application

import eventDemo.Tag
import eventDemo.contexts.auth.application.eventStores.UserRepository
import eventDemo.contexts.auth.domain.User
import eventDemo.contexts.game.application.channels.GameChannelsSubscriber
import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.domain.game.gameState.Game
import eventDemo.contexts.game.domain.game.gameState.GameStarted
import eventDemo.contexts.game.domain.game.gameState.disableRandomForTest
import eventDemo.shared.game.Card
import eventDemo.shared.game.command.GameCommand
import eventDemo.shared.game.notification.ItsTheTurnOfNotification
import eventDemo.shared.game.notification.Notification
import eventDemo.shared.game.notification.PlayerAsJoinTheGameNotification
import eventDemo.shared.game.notification.PlayerAsPlayACardNotification
import eventDemo.shared.game.notification.PlayerWasReadyNotification
import eventDemo.shared.game.notification.TheGameWasStartedNotification
import eventDemo.shared.game.notification.WelcomeToTheGameNotification
import eventDemo.shared.ids.GameId
import eventDemo.testHelpers.CreateGameWithCommandsInChannelsHelpers.createGameWithCommandsInChannels
import eventDemo.testHelpers.CreateGameWithCommandsInChannelsHelpers.joinTheGame
import eventDemo.testHelpers.CreateGameWithCommandsInChannelsHelpers.playCard
import eventDemo.testHelpers.CreateGameWithCommandsInChannelsHelpers.readyToPlay
import eventDemo.testHelpers.createNewUser
import eventDemo.testHelpers.testKoinApplicationWithConfig
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.until
import io.kotest.assertions.retry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.assertInstanceOf
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@DelicateCoroutinesApi
class GameSimulationTest :
  FunSpec({
    tags(Tag.Postgresql)

    test("Simulation of a game") {
      should {
        retry(maxRetry = 3, timeout = 20.seconds) {
          disableRandomForTest()
          val gameId = GameId()
          val user1 = createNewUser("user1")
          val user2 = createNewUser("user2")

          val channelCommand1 = Channel<GameCommand>(Channel.BUFFERED)
          val channelCommand2 = Channel<GameCommand>(Channel.BUFFERED)
          val channelNotification1 = Channel<Notification>(Channel.BUFFERED)
          val channelNotification2 = Channel<Notification>(Channel.BUFFERED)

          var playedCard1: Card? = null
          var playedCard2: Card? = null

          var player1HasJoin = false

          testKoinApplicationWithConfig {
            val gameRepository = get<GameRepository>()
            val userRepository = get<UserRepository>()
            userRepository.run {
              save(user1)
              save(user2)
            }

            gameRepository.create(gameId)

            // Run command/notification subscriber
            // In the normal process, these subscriber is invoque on players connect to the websocket
            GlobalScope.launch(Dispatchers.IO) {
              get<GameChannelsSubscriber>().subscribePlayerToGameChannels(
                gameId,
                user1.id,
                channelCommand1,
                channelNotification1,
              )
            }
            GlobalScope.launch(Dispatchers.IO) {
              get<GameChannelsSubscriber>().subscribePlayerToGameChannels(
                gameId,
                user2.id,
                channelCommand2,
                channelNotification2,
              )
            }

            // Consume etch notification of players, and put theses in a list.
            // Is used later to control when other players can execute the next action
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

            // Player 1 actions
            val player1Job =
              launch {
                createGameWithCommandsInChannels(channelCommand1, gameId, user1) {

                  joinTheGame()
                  player1Notifications.waitNotification<WelcomeToTheGameNotification> {
                    players.map { it.userId }.contains(user1.id)
                  }

                  player1HasJoin = true

                  player1Notifications.waitNotification<PlayerAsJoinTheGameNotification> {
                    player.userId == user2.id
                  }

                  readyToPlay()
                  player1Notifications.waitNotification<PlayerWasReadyNotification> {
                    playerId == getPlayer(user2).id
                  }

                  playedCard1 =
                    player1Notifications
                      .waitNotification<TheGameWasStartedNotification> { hand.size == 7 }
                      .hand
                      .first()
                      .apply {
                        this.shouldBeInstanceOf<Card.NumericCard>()
                        number shouldEqual 1
                        color shouldEqual Card.Color.Red
                      }

                  player1Notifications.waitNotification<ItsTheTurnOfNotification> {
                    if (player.userId == user2.id) error("WRONG PLAYER TURN")
                    player.userId == user1.id
                  }

                  game
                    .shouldBeInstanceOf<GameStarted>()
                    .discardPile
                    .topCard
                    .shouldNotBeNull()
                    .shouldBeInstanceOf<Card.NumericCard> {
                      it.number shouldEqual 0
                      it.color shouldEqual Card.Color.Red
                    }

                  playCard(playedCard1!!)

                  player1Notifications.waitNotification<ItsTheTurnOfNotification> {
                    player == getPlayer(user2)
                  }

                  player1Notifications.waitNotification<PlayerAsPlayACardNotification> {
                    playerId == getPlayer(user2).id && card == playedCard2
                  }

                  playedCard1 =
                    assertInstanceOf<GameStarted>(game)
                      .playableCards(currentPlayer.id)
                      .first()

                  playedCard1.run {
                    this.shouldBeInstanceOf<Card.NumericCard>()
                    number shouldEqual 2
                    color shouldEqual Card.Color.Red
                  }

                  playCard(playedCard1)

                  player1Notifications.waitNotification<ItsTheTurnOfNotification> {
                    player == getPlayer(user2)
                  }
                }
              }

            // Player 2 actions
            val player2Job =
              launch {
                createGameWithCommandsInChannels(channelCommand2, gameId, user2) {
                  // wait player 1 has joined the game
                  until(3.seconds) { player1HasJoin }

                  joinTheGame()

                  player2Notifications.waitNotification<WelcomeToTheGameNotification> {
                    players.map { it.userId }.contains(user1.id) &&
                      players.map { it.userId }.contains(user2.id)
                  }
                  player2Notifications.waitNotification<PlayerWasReadyNotification> { playerId == getPlayer(user1).id }

                  readyToPlay()

                  playedCard2 =
                    player2Notifications
                      .waitNotification<TheGameWasStartedNotification> { hand.size == 7 }
                      .hand
                      .first()
                      .apply {
                        this.shouldBeInstanceOf<Card.NumericCard>()
                        number shouldEqual 8
                        color shouldEqual Card.Color.Red
                      }

                  player2Notifications.waitNotification<ItsTheTurnOfNotification> {
                    if (player.userId == user2.id) error("WRONG PLAYER TURN")
                    player.userId == user1.id
                  }
                  player2Notifications.waitNotification<PlayerAsPlayACardNotification> {
                    playerId == getPlayer(user1).id && card == playedCard1
                  }

                  player2Notifications.waitNotification<ItsTheTurnOfNotification> {
                    player == currentPlayer
                  }

                  game
                    .shouldBeInstanceOf<GameStarted>()
                    .discardPile
                    .topCard
                    .shouldNotBeNull()
                    .shouldBeInstanceOf<Card.NumericCard> {
                      it.number shouldEqual 1
                      it.color shouldEqual Card.Color.Red
                    }

                  playCard(playedCard2)

                  player2Notifications.waitNotification<ItsTheTurnOfNotification> {
                    player.userId == user1.id
                  }
                  player2Notifications.waitNotification<PlayerAsPlayACardNotification> {
                    playerId == currentPlayer.id && card == playedCard2
                  }
                }
              }

            // Wait the end of the game
            joinAll(player1Job, player2Job)

            // Build the last state from the event store
            val game = gameRepository.get(gameId)
            assertInstanceOf<GameStarted>(game)

            // Check if the state is correct
            game.aggregateId shouldBeEqual gameId
            game.players.map { it.userId } shouldContainExactly setOf(user1.id, user2.id)
            assertNotNull(game.players.find { it.userId == user1.id })
              .hand.size shouldBeEqual 5
            assertNotNull(game.players.find { it.userId == user2.id })
              .hand.size shouldBeEqual 6
            game.direction shouldBeEqual Game.Direction.CLOCKWISE
            assertNotNull(game.lastPlayer?.userId) shouldBeEqual user1.id
            assertNotNull(game.discardPile.topCard) shouldBeEqual assertNotNull(playedCard1)
          }
        }
      }
    }
  })

context(user: User)
private suspend inline fun <reified T : Notification> MutableList<Notification>.waitNotification(crossinline block: T.() -> Boolean): T {
  println("NOTIFICATION WAITING: ${T::class.simpleName} for user: ${user.username}")
  return eventually(5.seconds) {
    filterIsInstance<T>()
      .first { block(it) }
      .also { remove(it) }
  }.also { println("NOTIFICATION RECEIVED: ${T::class.simpleName} for user: ${user.username}") }
}
