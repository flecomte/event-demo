package eventDemo.app.notification

import eventDemo.app.entity.Card
import eventDemo.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class YourNewCardNotification(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    val card: Card,
) : Notification
