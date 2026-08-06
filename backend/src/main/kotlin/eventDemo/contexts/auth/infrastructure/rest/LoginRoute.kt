package eventDemo.contexts.auth.infrastructure.rest

import eventDemo.contexts.auth.application.ports.UserProjectionRepository
import eventDemo.contexts.auth.infrastructure.configure.makeJwt
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.loginRoute(
  jwtSecret: String,
  userProjectionRepository: UserProjectionRepository,
) {
  post("login/{username}") {
    val username = call.parameters["username"]!!
    val rawPassword = call.parameters["password"]!!

    val userProjection =
      userProjectionRepository.getUserIfPasswordIsValid(username, rawPassword)
        ?: return@post call.respond(HttpStatusCode.BadRequest)

    call.respond(hashMapOf("token" to userProjection.makeJwt(jwtSecret)))
  }
}
