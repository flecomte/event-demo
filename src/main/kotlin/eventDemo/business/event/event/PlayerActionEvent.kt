package eventDemo.business.event.event

import eventDemo.business.entity.Player

sealed interface PlayerActionEvent : GameEvent {
  val player: Player
}
