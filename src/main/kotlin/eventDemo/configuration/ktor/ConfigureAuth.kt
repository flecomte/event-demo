package eventDemo.configuration.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eventDemo.business.entity.Player
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.util.Date

private const val JWT_ISSUER = "PlayCardGame"

fun Application.configureSecurity() {
  val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: error("You must set a jwt secret")

  authentication {
    jwt {
      realm = "Play card game"
      verifier(
        JWT
          .require(Algorithm.HMAC256(jwtSecret))
          .withIssuer(JWT_ISSUER)
          .build(),
      )
      validate { credential ->
        if (credential.payload.getClaim("username").asString() != "") {
          JWTPrincipal(credential.payload)
        } else {
          null
        }
      }
      challenge { _, _ ->
        call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
      }
    }
  }

  routing {
    post("login/{username}") {
      val username = call.parameters["username"]!!
      val player = Player(name = username)

      call.respond(hashMapOf("token" to player.makeJwt(jwtSecret)))
    }
  }
}

fun Player.makeJwt(jwtSecret: String): String =
  JWT
    .create()
    .withIssuer(JWT_ISSUER)
    .withClaim("username", name)
    .withPayload(Json.encodeToString(this))
    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
    .sign(Algorithm.HMAC256(jwtSecret))
