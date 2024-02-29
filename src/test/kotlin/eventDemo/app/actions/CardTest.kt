package eventDemo.app.actions

import eventDemo.app.Card
import eventDemo.app.EventStream
import eventDemo.app.GameId
import eventDemo.app.PlayCardEvent
import eventDemo.app.read
import eventDemo.module
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
                    module()
                }
                val id = GameId()
                val card: Card = Card.Simple(1, Card.Color.Blue)
                httpClient()
                    .post("/game/$id/card") {
                        contentType(Json)
                        accept(Json)
                        setBody(card)
                    }.apply {
                        assertEquals(HttpStatusCode.OK, status, message = bodyAsText())

                        val eventStream = getKoin().get<EventStream<GameId>>()
                        assertEquals(PlayCardEvent(id, card), eventStream.read<PlayCardEvent, GameId>(id))
                    }
            }
        }

        test("/game/{id}/card/last") {
            testApplication {
                val id = GameId()
                val card: Card = Card.Simple(1, Card.Color.Blue)
                application {
                    stopKoin()
                    module()
                    val eventStream by inject<EventStream<GameId>>()
                    eventStream.publish(
                        PlayCardEvent(GameId(), Card.Simple(2, Card.Color.Yellow)),
                        PlayCardEvent(id, card),
                    )
                }

                httpClient().get("/game/$id/card/last").apply {
                    assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
                    assertEquals(card, this.call.body<Card>())
                }
            }
        }
    })
