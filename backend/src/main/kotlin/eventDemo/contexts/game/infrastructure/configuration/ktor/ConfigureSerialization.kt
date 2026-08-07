package eventDemo.contexts.game.infrastructure.configuration.ktor

import eventDemo.shared.game.Player
import eventDemo.shared.ids.CommandId
import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.CommandIdSerializer
import eventDemo.shared.serializers.EventIdSerializer
import eventDemo.shared.serializers.GameIdSerializer
import eventDemo.shared.serializers.PlayerIdSerializer
import eventDemo.shared.serializers.UUIDSerializer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.uuid.Uuid

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
        contextual(Uuid::class) { UUIDSerializer }
        contextual(GameId::class) { GameIdSerializer }
        contextual(EventId::class) { EventIdSerializer }
        contextual(CommandId::class) { CommandIdSerializer }
        contextual(Player.PlayerId::class) { PlayerIdSerializer }
      }
  }
