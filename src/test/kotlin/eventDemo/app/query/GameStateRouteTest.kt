package eventDemo.app.query

import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.app.event.event.GameStartedEvent
import eventDemo.app.event.event.NewPlayerEvent
import eventDemo.app.event.event.PlayerReadyEvent
import eventDemo.app.event.projection.GameState
import eventDemo.app.event.projection.GameStateRepository
import eventDemo.configuration.configure
import eventDemo.configuration.makeJwt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.inject
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GameStateRouteTest :
    FunSpec({
        test("/game/{id}/state on empty game") {
            testApplication {
                val id = GameId()
                val player1 = Player(name = "Nikola")
                application {
                    stopKoin()
                    configure()
                }

                httpClient()
                    .get("/game/$id/state") {
                        withAuth(player1)
                        accept(ContentType.Application.Json)
                    }.apply {
                        assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
                        val state = call.body<GameState>()
                        assertEquals(id, state.gameId)
                        state.players shouldHaveSize 0
                        state.isStarted shouldBeEqual false
                    }
            }
        }

        test("/game/{id}/card/last") {
            testApplication {
                val gameId = GameId()
                val player1 = Player(name = "Nikola")
                val player2 = Player(name = "Einstein")
                var lastPlayedCard: Card? = null

                application {
                    stopKoin()
                    configure()

                    val eventHandler by inject<GameEventHandler>()
                    val stateRepo by inject<GameStateRepository>()
                    runBlocking {
                        eventHandler.handle(
                            NewPlayerEvent(gameId, player1),
                            NewPlayerEvent(gameId, player2),
                            PlayerReadyEvent(gameId, player1),
                            PlayerReadyEvent(gameId, player2),
                            GameStartedEvent.new(
                                gameId,
                                setOf(player1, player2),
                                shuffleIsDisabled = true,
                            ),
                        )
                        delay(100)
                        lastPlayedCard = stateRepo.get(gameId).playableCards(player1).first()
                        assertNotNull(lastPlayedCard)
                            .let { assertIs<Card.NumericCard>(lastPlayedCard) }
                            .let {
                                it.number shouldBeEqual 0
                                it.color shouldBeEqual Card.Color.Red
                            }
                        delay(100)
                        eventHandler.handle(
                            CardIsPlayedEvent(
                                gameId,
                                assertNotNull(lastPlayedCard),
                                player1,
                            ),
                        )
                        delay(100)
                    }
                }

                httpClient()
                    .get("/game/$gameId/card/last") {
                        withAuth(player1)
                        accept(ContentType.Application.Json)
                    }.apply {
                        assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
                        assertEquals(assertNotNull(lastPlayedCard), call.body<Card>())
                    }
            }
        }
    })

private fun HttpRequestBuilder.withAuth(player: Player) {
    header("Authorization", "Bearer ${player.makeJwt()}")
}
