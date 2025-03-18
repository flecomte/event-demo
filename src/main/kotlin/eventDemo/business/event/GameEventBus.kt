package eventDemo.business.event

import eventDemo.business.event.event.GameEvent
import eventDemo.libs.bus.Bus
import java.util.UUID

abstract class GameEventBus :
  Bus<GameEvent>,
  Comparable<GameEventBus> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameEventBus): Int =
    compareValues(instanceId, other.instanceId)
}
