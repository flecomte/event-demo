package eventDemo.configuration.ktor

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import kotlinx.serialization.Serializable

fun Application.configureHttpRouting() {
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
}

class BadRequestException(
  val httpError: HttpErrorBadRequest,
) : Exception()

@Serializable
class HttpErrorBadRequest(
  val title: String = HttpStatusCode.BadRequest.description,
  val invalidParams: List<InvalidParam> = emptyList(),
) {
  val statusCode: Int = HttpStatusCode.BadRequest.value

  @Serializable
  data class InvalidParam(
    val name: String,
    val reason: String,
  )
}
