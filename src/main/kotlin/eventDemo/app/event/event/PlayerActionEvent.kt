package eventDemo.app.event.event

import eventDemo.app.entity.Player

sealed interface PlayerActionEvent : GameEvent {
  val player: Player
}
