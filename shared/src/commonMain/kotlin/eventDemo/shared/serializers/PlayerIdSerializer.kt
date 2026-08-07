package eventDemo.shared.serializers

import eventDemo.shared.game.Player
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.uuid.Uuid

object PlayerIdSerializer : KSerializer<Player.PlayerId> {
  override fun deserialize(decoder: Decoder): Player.PlayerId =
    Player.PlayerId(Uuid.parse(decoder.decodeString()))

  override fun serialize(
    encoder: Encoder,
    value: Player.PlayerId,
  ) {
    encoder.encodeString(value.id.toString())
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PlayerId", PrimitiveKind.STRING)
}
