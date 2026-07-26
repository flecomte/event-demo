package eventDemo.contexts.game.application.notification.models

import eventDemo.contexts.game.domain.game.Player
import eventDemo.libs.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PlayerAsJoinTheGameNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val player: Player,
) : Notification
