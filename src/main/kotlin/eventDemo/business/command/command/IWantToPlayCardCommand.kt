package eventDemo.business.command.command

import eventDemo.business.entity.Card
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.libs.command.CommandId
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
data class IWantToPlayCardCommand(
  override val payload: Payload,
) : GameCommand {
  override val id: CommandId = CommandId()

  @Serializable
  data class Payload(
    override val aggregateId: GameId,
    override val player: Player,
    val card: Card,
  ) : GameCommand.Payload
}
