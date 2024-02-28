package eventDemo.app.actions

import eventDemo.app.Card
import eventDemo.app.EventStream
import eventDemo.app.GameId
import eventDemo.app.PlayCardEvent
import eventDemo.module
import io.kotest.core.spec.style.FunSpec
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.inject
import kotlin.test.assertEquals

class CardTest : FunSpec({
    test("/game/{id}/card") {
        testApplication {
            val client = httpClient()
            application {
                stopKoin()
                module()
            }
            val id = GameId().toString()
            client.post("/game/$id/card") {
                contentType(Json)
                accept(Json)
                setBody(Card.Simple(1, Card.Color.Blue))
            }.apply {
                assertEquals(status, HttpStatusCode.OK)
            }
        }
    }

    test("/game/{id}/card/last") {
        testApplication {
            val client = httpClient()
            val id = GameId()
            val card = Card.Simple(1, Card.Color.Blue)
            application {
                stopKoin()
                module()
                val eventStream by inject<EventStream<GameId>>()
                eventStream.publish(
                    PlayCardEvent(GameId(), Card.Simple(2, Card.Color.Yellow)),
                    PlayCardEvent(id, card),
                )
            }

            client.get("/game/$id/card/last").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals(card, this.call.body<Card>())
            }
        }
    }
})
