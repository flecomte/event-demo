package eventDemo.contexts.game.application.notification.models

import eventDemo.libs.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed interface Notification {
  @Serializable(with = UUIDSerializer::class)
  val id: UUID
}
