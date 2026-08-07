package eventDemo.shared.game.command

import eventDemo.shared.ids.CommandId
import eventDemo.shared.ids.GameId
import eventDemo.shared.ids.UserId
import eventDemo.shared.serializers.GameIdSerializer
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
data class JoinTheGameCommand(
  override val userId: UserId,
  override val payload: Payload,
) : GameCommand {
  override val id: CommandId = CommandId()

  @Serializable
  data class Payload(
    @Serializable(with = GameIdSerializer::class)
    override val aggregateId: GameId,
  ) : GameCommand.Payload
}
