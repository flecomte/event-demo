package eventDemo.app.query

import eventDemo.business.entity.Player
import eventDemo.configuration.ktor.makeJwt
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header

internal fun HttpRequestBuilder.withAuth(player: Player) {
  header("Authorization", "Bearer ${player.makeJwt("secret")}")
}
