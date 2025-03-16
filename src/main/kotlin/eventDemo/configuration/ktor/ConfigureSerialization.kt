package eventDemo.configuration.ktor

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.configuration.serializer.CommandIdSerializer
import eventDemo.configuration.serializer.GameIdSerializer
import eventDemo.configuration.serializer.PlayerIdSerializer
import eventDemo.configuration.serializer.UUIDSerializer
import eventDemo.libs.command.CommandId
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.UUID

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json(
      defaultJsonSerializer(),
    )
  }
}

fun defaultJsonSerializer(): Json =
  Json {
    serializersModule =
      SerializersModule {
        contextual(UUID::class) { UUIDSerializer }
        contextual(GameId::class) { GameIdSerializer }
        contextual(CommandId::class) { CommandIdSerializer }
        contextual(Player.PlayerId::class) { PlayerIdSerializer }
      }
  }
