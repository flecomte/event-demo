package eventDemo.configuration.serializer

import eventDemo.business.entity.Player
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

object PlayerIdSerializer : KSerializer<Player.PlayerId> {
  override fun deserialize(decoder: Decoder): Player.PlayerId =
    Player.PlayerId(UUID.fromString(decoder.decodeString()))

  override fun serialize(
    encoder: Encoder,
    value: Player.PlayerId,
  ) {
    encoder.encodeString(value.id.toString())
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PlayerId", PrimitiveKind.STRING)
}
