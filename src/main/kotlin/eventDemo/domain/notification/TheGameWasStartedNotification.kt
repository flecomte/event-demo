package eventDemo.domain.notification

import eventDemo.domain.entity.Card
import eventDemo.configuration.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TheGameWasStartedNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val hand: List<Card>,
) : Notification
