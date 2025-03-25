package eventDemo.business.event.event

import eventDemo.business.entity.Player
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlayerActionEvent : GameEvent {
  val player: Player
}
