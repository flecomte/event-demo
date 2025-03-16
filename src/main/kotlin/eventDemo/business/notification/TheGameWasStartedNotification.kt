package eventDemo.business.notification

import eventDemo.business.entity.Card
import eventDemo.configuration.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TheGameWasStartedNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val hand: List<Card>,
) : Notification
