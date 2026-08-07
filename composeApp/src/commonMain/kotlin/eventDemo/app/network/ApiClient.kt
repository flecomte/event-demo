package eventDemo.app.network

import eventDemo.shared.game.projection.GameList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Talks to the Ktor backend over plain HTTP.
 * `baseUrl` defaults to the dev Traefik route documented in doc/installation.md.
 */
class ApiClient(
  private val baseUrl: String = "http://api.traefik.me",
) {
  private var token: String? = null

  private val client =
    HttpClient {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
          },
        )
      }
    }

  val isAuthenticated: Boolean
    get() = token != null

  suspend fun login(
    username: String,
    password: String,
  ): Result<Unit> =
    runCatching {
      val response: Map<String, String> =
        client
          .post("$baseUrl/login/$username") {
            parameter("password", password)
          }.body()
      token = response["token"] ?: error("Missing token in login response")
    }

  suspend fun listGames(): Result<List<GameList>> =
    runCatching {
      val currentToken = token ?: error("Not authenticated")
      client
        .get("$baseUrl/games") {
          header(HttpHeaders.Authorization, "Bearer $currentToken")
        }.body()
    }

  fun logout() {
    token = null
  }
}
