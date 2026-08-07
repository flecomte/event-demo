package eventDemo.shared.game.command

import eventDemo.shared.command.Command
import eventDemo.shared.ids.GameId
import eventDemo.shared.ids.UserId
import eventDemo.shared.serializers.GameIdSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed interface GameCommand : Command {
  val userId: UserId
  val payload: Payload

  @Serializable
  sealed interface Payload {
    @Serializable(with = GameIdSerializer::class)
    val aggregateId: GameId
  }
}
