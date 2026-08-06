package eventDemo.shared.game.notification

import eventDemo.shared.game.Player
import eventDemo.shared.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class WelcomeToTheGameNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: Uuid = Uuid.random(),
  val players: Set<Player>,
) : Notification
