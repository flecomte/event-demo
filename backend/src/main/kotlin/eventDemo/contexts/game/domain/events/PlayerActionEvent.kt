package eventDemo.contexts.game.domain.events

import eventDemo.shared.game.Player
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlayerActionEvent : GameEvent {
  val playerId: Player.PlayerId
}
