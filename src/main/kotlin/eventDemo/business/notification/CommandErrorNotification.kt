package eventDemo.business.notification

import eventDemo.configuration.serializer.UUIDSerializer
import eventDemo.libs.command.Command
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CommandErrorNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val message: String,
  val command: Command,
) : Notification,
  CommandNotification
