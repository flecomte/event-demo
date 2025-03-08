package eventDemo.app.query

import eventDemo.app.GameState
import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.CardIsPlayedEvent
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
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.inject
import kotlin.test.assertEquals

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
                val id = GameId()
                val card: Card = Card.NumericCard(1, Card.Color.Blue)
                val player = Player(name = "Nikola")

                application {
                    stopKoin()
                    configure()

                    val eventStream by inject<GameEventStream>()
                    eventStream.publish(
                        CardIsPlayedEvent(id, Card.NumericCard(2, Card.Color.Yellow), player),
                        CardIsPlayedEvent(id, card, player),
                        // Other game
                        CardIsPlayedEvent(GameId(), Card.NumericCard(2, Card.Color.Yellow), player),
                    )
                }

                httpClient()
                    .get("/game/$id/card/last") {
                        withAuth(player)
                        accept(ContentType.Application.Json)
                    }.apply {
                        assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
                        assertEquals(card, call.body<Card>())
                    }
            }
        }
    })

private fun HttpRequestBuilder.withAuth(player: Player) {
    header("Authorization", "Bearer ${player.makeJwt()}")
}
