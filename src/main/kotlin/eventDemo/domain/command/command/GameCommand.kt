package eventDemo.domain.command.command

import eventDemo.domain.entity.GameId
import eventDemo.domain.entity.Player
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
