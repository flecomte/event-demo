package eventDemo.domain.event.event

import eventDemo.domain.entity.Player
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlayerActionEvent : GameEvent {
  val player: Player
}
