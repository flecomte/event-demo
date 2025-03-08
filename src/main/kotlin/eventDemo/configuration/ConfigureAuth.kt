package eventDemo.configuration

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import eventDemo.app.entity.Player
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.util.Date

// TODO: read the jwt property from the config file
private val jwtRealm = "Play card game"
private val jwtIssuer = "PlayCardGame"
private val jwtSecret = "secret"

fun Application.configureSecurity() {
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build(),
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    routing {
        post("login/{username}") {
            val username = call.parameters["username"]!!
            val player = Player(name = username)

            call.respond(hashMapOf("token" to player.makeJwt()))
        }
    }
}

fun Player.makeJwt(): String =
    JWT
        .create()
        .withIssuer(jwtIssuer)
        .withClaim("username", name)
        .withPayload(Json.encodeToString(this))
        .withExpiresAt(Date(System.currentTimeMillis() + 60000))
        .sign(Algorithm.HMAC256(jwtSecret))
