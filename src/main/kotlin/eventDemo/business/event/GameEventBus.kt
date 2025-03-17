package eventDemo.business.event

import eventDemo.business.entity.GameId
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.event.EventBus
import java.util.UUID

abstract class GameEventBus :
  EventBus<GameEvent, GameId>,
  Comparable<GameEventBus> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameEventBus): Int =
    compareValues(instanceId, other.instanceId)
}
