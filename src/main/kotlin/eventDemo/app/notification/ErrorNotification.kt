package eventDemo.app.notification

import eventDemo.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ErrorNotification(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    val message: String,
) : Notification
