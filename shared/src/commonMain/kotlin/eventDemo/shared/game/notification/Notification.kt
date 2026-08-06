package eventDemo.shared.game.notification

import eventDemo.shared.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface Notification {
  @Serializable(with = UUIDSerializer::class)
  val id: Uuid
}
