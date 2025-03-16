package eventDemo.business.notification

import eventDemo.configuration.serializer.UUIDSerializer
import eventDemo.libs.command.CommandId
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CommandSuccessNotification(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
  val commandId: CommandId,
) : Notification,
  CommandNotification
