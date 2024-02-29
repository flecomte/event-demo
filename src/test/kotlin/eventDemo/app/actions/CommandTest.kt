package eventDemo.app.actions

import eventDemo.app.Card
import eventDemo.app.Command
import eventDemo.app.CommandStream
import eventDemo.app.Game
import eventDemo.app.PlayCardCommand
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

class CommandTest : FunSpec({
    test("/command/send") {
        testApplication {
            val client = httpClient()
            application {
                stopKoin()
                module()
            }
            val command = PlayCardCommand(Game.new(), Card.Simple(1, Card.Color.Blue))
            client.post("/command/send") {
                contentType(Json)
                accept(Json)
                setBody(command)
            }.apply {
                assertEquals(HttpStatusCode.OK, status, message = bodyAsText())

                val commandStream = getKoin().get<CommandStream>()
                assertEquals(command, commandStream.readNext())
            }
        }
    }

    test("/command/next") {
        testApplication {
            val command =
                PlayCardCommand(
                    Game.new(),
                    Card.Simple(1, Card.Color.Blue),
                )
            application {
                stopKoin()
                module()

                val commandStream by inject<CommandStream>()
                commandStream.sendRequest(command)
            }

            httpClient().get("/command/next").apply {
                assertEquals(HttpStatusCode.OK, status, message = bodyAsText())
                assertEquals(command, this.call.body<Command>())
            }
        }
    }
})
