package eventDemo.domain.command.command

import eventDemo.domain.entity.Card
import eventDemo.domain.entity.GameId
import eventDemo.domain.entity.Player
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
