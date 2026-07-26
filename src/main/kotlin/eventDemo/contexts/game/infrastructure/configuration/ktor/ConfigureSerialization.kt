package eventDemo.contexts.game.infrastructure.configuration.ktor

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.infrastructure.persistence.serializers.CommandIdSerializer
import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.contexts.game.infrastructure.persistence.serializers.GameIdSerializer
import eventDemo.contexts.game.infrastructure.persistence.serializers.PlayerIdSerializer
import eventDemo.libs.command.CommandId
import eventDemo.libs.eventSource.EventId
import eventDemo.libs.serializer.UUIDSerializer
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
        contextual(EventId::class) { EventIdSerializer }
        contextual(CommandId::class) { CommandIdSerializer }
        contextual(Player.PlayerId::class) { PlayerIdSerializer }
      }
  }
