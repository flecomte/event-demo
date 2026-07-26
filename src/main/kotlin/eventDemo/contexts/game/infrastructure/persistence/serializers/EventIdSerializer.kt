package eventDemo.contexts.game.infrastructure.persistence.serializers

import eventDemo.libs.eventSource.EventId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

object EventIdSerializer : KSerializer<EventId> {
  override fun deserialize(decoder: Decoder): EventId =
    EventId(UUID.fromString(decoder.decodeString()))

  override fun serialize(
    encoder: Encoder,
    value: EventId,
  ) {
    encoder.encodeString(value.id.toString())
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EventId", PrimitiveKind.STRING)
}
