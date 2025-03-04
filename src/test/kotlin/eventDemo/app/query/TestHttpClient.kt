package eventDemo.app.query

import eventDemo.shared.UUIDSerializer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.UUID

fun ApplicationTestBuilder.httpClient(): HttpClient =
    createClient {
        install(ContentNegotiation) {
            json(
                Json {
                    serializersModule =
                        SerializersModule {
                            contextual(UUID::class) { UUIDSerializer }
                        }
                },
            )
        }
    }
