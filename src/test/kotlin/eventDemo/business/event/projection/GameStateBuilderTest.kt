package eventDemo.business.event.projection

import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.event.CardIsPlayedEvent
import eventDemo.business.event.event.GameStartedEvent
import eventDemo.business.event.event.NewPlayerEvent
import eventDemo.business.event.event.PlayerReadyEvent
import eventDemo.business.event.event.disableShuffleDeck
import eventDemo.business.event.projection.gameState.GameState
import eventDemo.business.event.projection.gameState.apply
import eventDemo.libs.event.VersionBuilderLocal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GameStateBuilderTest :
  FunSpec({
    test("apply") {
      disableShuffleDeck()
      val versionBuilder = VersionBuilderLocal()
      val gameId = GameId()
      val player1 = Player(name = "Nikola")
      val player2 = Player(name = "Einstein")

      GameState(gameId)
        .run {
          val event =
            NewPlayerEvent(
              aggregateId = gameId,
              player = player1,
              version = versionBuilder.buildNextVersion(gameId),
            )
          apply(event).also { state ->
            state.aggregateId shouldBeEqual gameId
            state.isReady shouldBeEqual false
            state.isStarted shouldBeEqual false
          }
        }.run {
          val event =
            NewPlayerEvent(
              aggregateId = gameId,
              player = player2,
              version = versionBuilder.buildNextVersion(gameId),
            )
          apply(event).also { state ->
            state.aggregateId shouldBeEqual gameId
            state.players shouldBeEqual setOf(player1, player2)
          }
        }.run {
          val event =
            PlayerReadyEvent(
              aggregateId = gameId,
              player = player1,
              version = versionBuilder.buildNextVersion(gameId),
            )
          apply(event).also { state ->
            state.aggregateId shouldBeEqual gameId
            state.readyPlayers shouldBeEqual setOf(player1)
          }
        }.run {
          val event =
            PlayerReadyEvent(
              aggregateId = gameId,
              player = player2,
              version = versionBuilder.buildNextVersion(gameId),
            )
          apply(event).also { state ->
            state.aggregateId shouldBeEqual gameId
            state.readyPlayers shouldBeEqual setOf(player1, player2)
            state.isReady shouldBeEqual true
            state.isStarted shouldBeEqual false
          }
        }.run {
          val event =
            GameStartedEvent.new(
              id = gameId,
              players = setOf(player1, player2),
              shuffleIsDisabled = true,
              version = versionBuilder.buildNextVersion(gameId),
            )
          apply(event).also { state ->
            state.aggregateId shouldBeEqual gameId
            state.isStarted shouldBeEqual true
            assertIs<Card.NumericCard>(state.deck.stack.first()).let {
              it.number shouldBeEqual 6
              it.color shouldBeEqual Card.Color.Red
            }
          }
        }.run {
          val playedCard = playableCards(player1)[0]
          val event =
            CardIsPlayedEvent(
              aggregateId = gameId,
              card = playedCard,
              player = player1,
              version = versionBuilder.buildNextVersion(gameId),
            )
          apply(event).also { state ->
            state.aggregateId shouldBeEqual gameId
            assertNotNull(state.cardOnCurrentStack) shouldBeEqual playedCard
            assertIs<Card.NumericCard>(playedCard).let {
              it.number shouldBeEqual 0
              it.color shouldBeEqual Card.Color.Red
            }
          }
        }.run {
          val playedCard = playableCards(player2)[0]
          val event =
            CardIsPlayedEvent(
              aggregateId = gameId,
              card = playedCard,
              player = player2,
              version = versionBuilder.buildNextVersion(gameId),
            )
          apply(event).also { state ->
            state.aggregateId shouldBeEqual gameId
            assertNotNull(state.cardOnCurrentStack) shouldBeEqual playedCard
            assertIs<Card.NumericCard>(playedCard).let {
              it.number shouldBeEqual 7
              it.color shouldBeEqual Card.Color.Red
            }
          }
        }
    }
  })
