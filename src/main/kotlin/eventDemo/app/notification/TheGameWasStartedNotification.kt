package eventDemo.app.notification

import eventDemo.app.entity.Card
import eventDemo.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TheGameWasStartedNotification(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    val hand: List<Card>,
) : Notification
