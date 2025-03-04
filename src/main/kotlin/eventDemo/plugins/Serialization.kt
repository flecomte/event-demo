package eventDemo.plugins

import eventDemo.libs.command.CommandId
import eventDemo.shared.GameId
import eventDemo.shared.entity.Player.PlayerId
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.UUID

fun Application.configureSerialization() {
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

object CommandIdSerializer : KSerializer<CommandId> {
    override fun deserialize(decoder: Decoder): CommandId = CommandId(decoder.decodeString())

    override fun serialize(
        encoder: Encoder,
        value: CommandId,
    ) {
        encoder.encodeString(value.toString())
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CommandId", PrimitiveKind.STRING)
}

object PlayerIdSerializer : KSerializer<PlayerId> {
    override fun deserialize(decoder: Decoder): PlayerId = PlayerId(UUID.fromString(decoder.decodeString()))

    override fun serialize(
        encoder: Encoder,
        value: PlayerId,
    ) {
        encoder.encodeString(value.id.toString())
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GameId", PrimitiveKind.STRING)
}

object GameIdSerializer : KSerializer<GameId> {
    override fun deserialize(decoder: Decoder): GameId = GameId(UUID.fromString(decoder.decodeString()))

    override fun serialize(
        encoder: Encoder,
        value: GameId,
    ) {
        encoder.encodeString(value.id.toString())
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GameId", PrimitiveKind.STRING)
}

object UUIDSerializer : KSerializer<UUID> {
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

    override fun serialize(
        encoder: Encoder,
        value: UUID,
    ) {
        encoder.encodeString(value.toString())
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
}
