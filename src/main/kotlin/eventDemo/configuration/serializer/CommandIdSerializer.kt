package eventDemo.configuration.serializer

import eventDemo.libs.command.CommandId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CommandIdSerializer : KSerializer<CommandId> {
  override fun deserialize(decoder: Decoder): CommandId =
    CommandId(decoder.decodeString())

  override fun serialize(
    encoder: Encoder,
    value: CommandId,
  ) {
    encoder.encodeString(value.toString())
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CommandId", PrimitiveKind.STRING)
}
