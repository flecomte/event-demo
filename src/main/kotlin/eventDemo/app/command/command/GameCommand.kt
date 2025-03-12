package eventDemo.app.command.command

import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
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
