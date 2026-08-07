package eventDemo.contexts.auth.infrastructure.configure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eventDemo.configuration.configuration
import eventDemo.contexts.auth.domain.User
import eventDemo.contexts.auth.infrastructure.persistence.projection.UserProjection
import eventDemo.shared.ids.UserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import java.util.Date

fun Application.configureKtorAuth() {
  val jwtSecret = environment.config.configuration.jwtSecret
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
        if (credential.payload
            .getClaim("username")
            .asString()
            .isNotEmpty()
        ) {
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
}

private const val JWT_ISSUER = "PlayCardGame"

fun UserProjection.makeJwt(jwtSecret: String): String =
  makeJwt(jwtSecret, id, username)

fun User.makeJwt(jwtSecret: String): String =
  makeJwt(jwtSecret, id, username)

fun makeJwt(
  jwtSecret: String,
  id: UserId,
  username: String,
): String =
  JWT
    .create()
    .withIssuer(JWT_ISSUER)
    .withClaim("username", username)
    .withClaim("userid", id.toString())
    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
    .sign(Algorithm.HMAC256(jwtSecret))
