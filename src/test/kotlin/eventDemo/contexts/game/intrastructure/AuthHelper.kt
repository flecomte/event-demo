package eventDemo.contexts.game.intrastructure

import eventDemo.contexts.auth.domain.User
import eventDemo.contexts.auth.infrastructure.configure.makeJwt
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

internal fun HttpRequestBuilder.withAuth(user: User) {
  header("Authorization", "Bearer ${user.makeJwt("secret")}")
}
