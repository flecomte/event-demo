package eventDemo.app.actions

import eventDemo.app.Command
import eventDemo.app.CommandId
import eventDemo.app.CommandStream
import eventDemo.app.PlayCardCommand
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
@Resource("/command")
class CommandRoute {
    @Resource("send")
    class Send(
        val commandRoute: CommandRoute,
    ) {
        @Serializable
        data class Response(
            val id: CommandId,
        ) {
            constructor(command: Command) : this(command.id)
        }
    }

    @Resource("next")
    class Next(
        val commandRoute: CommandRoute,
    )
}

fun Routing.command() {
    val commandStream by inject<CommandStream>()

    post<CommandRoute.Send> {
        val command = call.receive<PlayCardCommand>()
        commandStream.sendRequest(command)
        call.respond(HttpStatusCode.OK, CommandRoute.Send.Response(command))
    }

    get<CommandRoute.Next> {
        val command = commandStream.readNext()
        if (command == null) {
            call.response.status(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.OK, command)
        }
    }
}
