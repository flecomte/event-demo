package eventDemo.sharedKernel

import eventDemo.libs.eventSource.AggregateId
import eventDemo.libs.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@JvmInline
value class UserId(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
) : AggregateId
