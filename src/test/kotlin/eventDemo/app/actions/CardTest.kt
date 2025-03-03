package eventDemo.app.actions

import eventDemo.configure
import eventDemo.shared.GameId
import eventDemo.shared.entity.Card
import eventDemo.shared.event.CardIsPlayedEvent
import eventDemo.shared.event.GameEventStream
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
                httpClient()
                    .post("/game/$id/card") {
                        contentType(Json)
                        accept(Json)
                        setBody(card)
                    }.apply {
                        assertEquals(HttpStatusCode.OK, status, message = bodyAsText())

                        val eventStream = getKoin().get<GameEventStream>()
                        assertEquals(CardIsPlayedEvent(id, card), eventStream.readLast(id))
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
                    eventStream.publish(
                        CardIsPlayedEvent(id, Card.NumericCard(2, Card.Color.Yellow)),
                        CardIsPlayedEvent(id, card),
                        // Other game
                        CardIsPlayedEvent(GameId(), Card.NumericCard(2, Card.Color.Yellow)),
                    )
                }

                httpClient().get("/game/$id/card/last").apply {
                    assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
                    assertEquals(card, call.body<Card>())
                }
            }
        }
    })
