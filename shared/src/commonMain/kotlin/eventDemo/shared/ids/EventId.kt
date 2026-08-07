package eventDemo.shared.ids

import eventDemo.shared.serializers.UUIDSerializer
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class EventId(
  @Serializable(with = UUIDSerializer::class)
  val id: Uuid = Uuid.random(),
)
