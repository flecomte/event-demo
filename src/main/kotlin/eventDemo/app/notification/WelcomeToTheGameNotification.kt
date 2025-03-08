package eventDemo.app.notification

import eventDemo.app.entity.Player
import eventDemo.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WelcomeToTheGameNotification(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    val players: Set<Player>,
) : Notification
