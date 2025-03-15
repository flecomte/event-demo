package eventDemo.app.command.command

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.libs.command.CommandId
import kotlinx.serialization.Serializable

/**
 * A command to set as ready to play
 */
@Serializable
data class IamReadyToPlayCommand(
  override val payload: Payload,
) : GameCommand {
  override val id: CommandId = CommandId()

  @Serializable
  data class Payload(
    override val aggregateId: GameId,
    override val player: Player,
  ) : GameCommand.Payload
}
