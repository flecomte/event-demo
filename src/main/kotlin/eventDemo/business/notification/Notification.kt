package eventDemo.business.notification

import eventDemo.configuration.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed interface Notification {
  @Serializable(with = UUIDSerializer::class)
  val id: UUID
}
