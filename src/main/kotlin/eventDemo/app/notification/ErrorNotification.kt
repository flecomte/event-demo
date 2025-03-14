package eventDemo.app.notification

import eventDemo.configuration.UUIDSerializer
import eventDemo.libs.command.Command
import kotlinx.serialization.Serializable
import java.util.UUID

sealed interface CommandStateNotification : Notification

@Serializable
data class ErrorNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val message: String,
  val command: Command,
) : Notification,
  CommandStateNotification
