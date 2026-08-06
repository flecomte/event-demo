package eventDemo.shared.ids

import eventDemo.shared.serializers.UUIDSerializer
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class UserId(
  @Serializable(with = UUIDSerializer::class)
  override val id: Uuid = Uuid.random(),
) : AggregateId
