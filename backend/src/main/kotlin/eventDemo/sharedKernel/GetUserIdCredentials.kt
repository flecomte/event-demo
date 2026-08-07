package eventDemo.sharedKernel

import eventDemo.shared.ids.UserId
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import kotlin.uuid.Uuid

internal val ApplicationCall.currentUserId: UserId
  get() =
    principal<JWTPrincipal>()!!.run {
      UserId(Uuid.parse(payload.getClaim("userid").asString()))
    }
