package eventDemo.contexts.game.domain.events

import eventDemo.contexts.game.domain.game.Player
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlayerActionEvent : GameEvent {
  val playerId: Player.PlayerId
}
