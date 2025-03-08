package eventDemo.app.notification

import eventDemo.app.entity.Card
import eventDemo.app.entity.Player
import eventDemo.shared.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PlayerWasChoseTheCardColorNotification(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    val player: Player,
    val color: Card.Color,
) : Notification
