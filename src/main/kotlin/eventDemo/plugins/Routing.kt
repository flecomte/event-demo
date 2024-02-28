package eventDemo.plugins

import eventDemo.app.actions.putCard
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.util.converters.DataConversion.Configuration

fun Application.configureRouting() {
    install(AutoHeadResponse)
    install(Resources)
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        putCard()
    }
}

private typealias ConverterDeclaration = Configuration.() -> Unit
