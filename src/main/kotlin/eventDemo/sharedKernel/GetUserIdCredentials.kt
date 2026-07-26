package eventDemo.sharedKernel

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

internal val ApplicationCall.currentUserId: UserId
  get() =
    principal<JWTPrincipal>()!!.run {
      UserId(UUID.fromString(payload.getClaim("userid").asString()))
    }
