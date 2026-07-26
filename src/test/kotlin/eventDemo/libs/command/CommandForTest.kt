package eventDemo.libs.command

import kotlinx.serialization.Serializable

@Serializable
data class CommandForTest(
  override val id: CommandId,
) : Command
