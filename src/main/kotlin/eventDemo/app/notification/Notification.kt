package eventDemo.app.notification

import eventDemo.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed interface Notification {
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
}
