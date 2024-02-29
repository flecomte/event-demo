package eventDemo.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}

class BadRequestException(
    val httpError: HttpErrorBadRequest,
) : Exception()

class HttpErrorBadRequest(
    statusCode: HttpStatusCode,
    val title: String = statusCode.description,
    val invalidParams: List<InvalidParam>,
) {
    val statusCode: Int = statusCode.value

    data class InvalidParam(
        val name: String,
        val reason: String,
    )
}
