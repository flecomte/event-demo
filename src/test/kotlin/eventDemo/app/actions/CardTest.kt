package eventDemo.app.actions

import eventDemo.app.GameId
import eventDemo.app.entity.Card
import eventDemo.app.entity.Player
import eventDemo.app.event.CardIsPlayedEvent
import eventDemo.app.event.GameEventStream
import eventDemo.configuration.configure
import io.kotest.core.spec.style.FunSpec
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.ktor.ext.inject
import kotlin.test.assertEquals

class CardTest :
    FunSpec({
        test("/game/{id}/card") {
            testApplication {
                application {
                    stopKoin()
                    configure()
                }

                val id = GameId()
                val card: Card = Card.NumericCard(1, Card.Color.Blue)
                val player = Player(name = "Nikola")
                httpClient()
                    .post("/game/$id/card") {
                        contentType(Json)
                        accept(Json)
                        setBody(card)
                    }.apply {
                        assertEquals(HttpStatusCode.OK, status, message = bodyAsText())

                        val eventStream = getKoin().get<GameEventStream>()
                        assertEquals(CardIsPlayedEvent(id, card, player), eventStream.readLast(id))
                    }
            }
        }

        test("/game/{id}/card/last") {
            testApplication {
                val id = GameId()
                val card: Card = Card.NumericCard(1, Card.Color.Blue)
                application {
                    stopKoin()
                    configure()

                    val eventStream by inject<GameEventStream>()
                    val player = Player(name = "Nikola")
                    eventStream.publish(
                        CardIsPlayedEvent(id, Card.NumericCard(2, Card.Color.Yellow), player),
                        CardIsPlayedEvent(id, card, player),
                        // Other game
                        CardIsPlayedEvent(GameId(), Card.NumericCard(2, Card.Color.Yellow), player),
                    )
                }

                httpClient().get("/game/$id/card/last").apply {
                    assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
                    assertEquals(card, call.body<Card>())
                }
            }
        }
    })
