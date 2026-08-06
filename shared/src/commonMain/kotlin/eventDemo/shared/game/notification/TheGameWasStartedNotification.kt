package eventDemo.shared.game.notification

import eventDemo.shared.game.Card
import eventDemo.shared.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class TheGameWasStartedNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: Uuid = Uuid.random(),
  val hand: Set<Card>,
) : Notification
