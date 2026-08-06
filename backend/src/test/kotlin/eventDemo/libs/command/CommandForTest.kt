package eventDemo.libs.command

import eventDemo.shared.command.Command
import eventDemo.shared.ids.CommandId
import kotlinx.serialization.Serializable

@Serializable
data class CommandForTest(
  override val id: CommandId,
) : Command
