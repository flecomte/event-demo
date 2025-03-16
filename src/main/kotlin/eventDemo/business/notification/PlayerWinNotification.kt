package eventDemo.business.notification

import eventDemo.business.entity.Player
import eventDemo.configuration.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PlayerWinNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val player: Player,
) : Notification
