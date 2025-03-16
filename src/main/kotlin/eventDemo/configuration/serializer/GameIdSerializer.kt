package eventDemo.configuration.serializer

import eventDemo.business.entity.GameId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

object GameIdSerializer : KSerializer<GameId> {
  override fun deserialize(decoder: Decoder): GameId =
    GameId(UUID.fromString(decoder.decodeString()))

  override fun serialize(
    encoder: Encoder,
    value: GameId,
  ) {
    encoder.encodeString(value.id.toString())
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GameId", PrimitiveKind.STRING)
}
