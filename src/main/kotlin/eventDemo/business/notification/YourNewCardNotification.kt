package eventDemo.business.notification

import eventDemo.business.entity.Card
import eventDemo.configuration.serializer.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class YourNewCardNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val card: Card,
) : Notification
