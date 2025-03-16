package eventDemo.app.query
import eventDemo.configuration.ktor.defaultJsonSerializer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder

fun ApplicationTestBuilder.httpClient(): HttpClient =
  createClient {
    install(ContentNegotiation) {
      json(
        defaultJsonSerializer(),
      )
    }
  }
