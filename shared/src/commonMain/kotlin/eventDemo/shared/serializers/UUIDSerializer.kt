package eventDemo.shared.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid

object UUIDSerializer : KSerializer<Uuid> {
  override fun deserialize(decoder: Decoder): Uuid =
    Uuid.parse(decoder.decodeString())

  override fun serialize(
    encoder: Encoder,
    value: Uuid,
  ) {
    encoder.encodeString(value.toString())
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
}
