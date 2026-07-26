package eventDemo.contexts.game.application.command.models

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.Player
import eventDemo.contexts.game.infrastructure.persistence.serializers.GameIdSerializer
import eventDemo.contexts.game.infrastructure.persistence.serializers.PlayerIdSerializer
import eventDemo.libs.command.CommandId
import eventDemo.sharedKernel.UserId
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
data class TakeCartFromDrawPileCommand(
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
