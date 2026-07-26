package eventDemo.contexts.game.application.notification.models

import eventDemo.libs.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PilesShuffledNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
) : Notification
