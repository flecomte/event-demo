package eventDemo.domain.notification

import eventDemo.domain.entity.Player
import eventDemo.configuration.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ItsTheTurnOfNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val player: Player,
) : Notification
