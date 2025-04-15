package eventDemo.adapter.interfaceLayer.query

import eventDemo.business.entity.Player
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

internal fun ApplicationCall.getPlayerCredentials() =
  principal<JWTPrincipal>()!!.run {
    Player(
      id = payload.getClaim("playerid").asString(),
      name = payload.getClaim("username").asString(),
    )
  }
