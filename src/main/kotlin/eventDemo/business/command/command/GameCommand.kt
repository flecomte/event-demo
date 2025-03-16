package eventDemo.business.command.command

import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.libs.command.Command
import kotlinx.serialization.Serializable

@Serializable
sealed interface GameCommand : Command {
  val payload: Payload

  @Serializable
  sealed interface Payload {
    val aggregateId: GameId
    val player: Player
  }
}
