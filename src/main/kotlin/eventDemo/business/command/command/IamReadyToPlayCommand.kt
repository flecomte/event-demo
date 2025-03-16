package eventDemo.business.command.command

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
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
