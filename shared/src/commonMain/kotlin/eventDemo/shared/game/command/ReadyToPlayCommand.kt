package eventDemo.shared.game.command

import eventDemo.shared.game.Player
import eventDemo.shared.ids.CommandId
import eventDemo.shared.ids.GameId
import eventDemo.shared.ids.UserId
import eventDemo.shared.serializers.GameIdSerializer
import eventDemo.shared.serializers.PlayerIdSerializer
import kotlinx.serialization.Serializable

/**
 * A command to set as ready to play
 */
@Serializable
data class ReadyToPlayCommand(
  override val userId: UserId,
  override val payload: Payload,
) : GameCommand {
  override val id: CommandId = CommandId()

  @Serializable
  data class Payload(
    @Serializable(with = GameIdSerializer::class)
    override val aggregateId: GameId,
    @Serializable(with = PlayerIdSerializer::class)
    val playerId: Player.PlayerId,
  ) : GameCommand.Payload
}
