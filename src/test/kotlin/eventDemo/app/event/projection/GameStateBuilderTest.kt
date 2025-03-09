package eventDemo.app.event.projection

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.event.disableShuffleDeck
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GameStateBuilderTest :
    FunSpec({
        test("apply") {
            disableShuffleDeck()
            val gameId = GameId()
            val player1 = Player(name = "Nikola")
            val player2 = Player(name = "Einstein")

            GameState(gameId)
                .run {
                    val event = NewPlayerEvent(gameId, player1)
                    apply(event).also { state ->
                        state.gameId shouldBeEqual gameId
                        state.isReady shouldBeEqual false
                        state.isStarted shouldBeEqual false
                    }
                }.run {
                    val event = NewPlayerEvent(gameId, player2)
                    apply(event).also { state ->
                        state.gameId shouldBeEqual gameId
                        state.players shouldBeEqual setOf(player1, player2)
                    }
                }.run {
                    val event = PlayerReadyEvent(gameId, player1)
                    apply(event).also { state ->
                        state.gameId shouldBeEqual gameId
                        state.readyPlayers shouldBeEqual setOf(player1)
                    }
                }.run {
                    val event = PlayerReadyEvent(gameId, player2)
                    apply(event).also { state ->
                        state.gameId shouldBeEqual gameId
                        state.readyPlayers shouldBeEqual setOf(player1, player2)
                        state.isReady shouldBeEqual true
                        state.isStarted shouldBeEqual false
                    }
                }.run {
                    val event =
                        GameStartedEvent.new(
                            gameId,
                            setOf(player1, player2),
                            shuffleIsDisabled = true,
                        )
                    apply(event).also { state ->
                        state.gameId shouldBeEqual gameId
                        state.isStarted shouldBeEqual true
                        assertIs<Card.NumericCard>(state.deck.stack.first()).let {
                            it.number shouldBeEqual 6
                            it.color shouldBeEqual Card.Color.Red
                        }
                    }
                }.run {
                    val playedCard = playableCards(player1)[0]
                    val event = CardIsPlayedEvent(gameId, playedCard, player1)
                    apply(event).also { state ->
                        state.gameId shouldBeEqual gameId
                        assertNotNull(state.lastCard).card shouldBeEqual playedCard
                        assertIs<Card.NumericCard>(playedCard).let {
                            it.number shouldBeEqual 0
                            it.color shouldBeEqual Card.Color.Red
                        }
                    }
                }.run {
                    val playedCard = playableCards(player2)[0]
                    val event = CardIsPlayedEvent(gameId, playedCard, player2)
                    apply(event).also { state ->
                        state.gameId shouldBeEqual gameId
                        assertNotNull(state.lastCard).card shouldBeEqual playedCard
                        assertIs<Card.NumericCard>(playedCard).let {
                            it.number shouldBeEqual 7
                            it.color shouldBeEqual Card.Color.Red
                        }
                    }
                }
        }
    })
