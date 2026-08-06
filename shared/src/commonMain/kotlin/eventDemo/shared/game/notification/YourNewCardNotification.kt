package eventDemo.shared.game.notification

import eventDemo.shared.game.Card
import eventDemo.shared.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class YourNewCardNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: Uuid = Uuid.random(),
  val cards: Set<Card>,
) : Notification
