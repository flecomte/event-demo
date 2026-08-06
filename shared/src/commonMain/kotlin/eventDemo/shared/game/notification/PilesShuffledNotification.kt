package eventDemo.shared.game.notification

import eventDemo.shared.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PilesShuffledNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: Uuid = Uuid.random(),
) : Notification
