package eventDemo.contexts.game.application.command.models

import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.infrastructure.persistence.serializers.GameIdSerializer
import eventDemo.libs.command.Command
import eventDemo.sharedKernel.UserId
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
