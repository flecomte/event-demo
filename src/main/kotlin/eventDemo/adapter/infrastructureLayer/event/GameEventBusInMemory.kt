package eventDemo.adapter.infrastructureLayer.event

import eventDemo.business.event.GameEventBus
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInMemory
import java.util.UUID

class GameEventBusInMemory :
  GameEventBus,
  Bus<GameEvent> by BusInMemory(),
  Comparable<GameEventBusInMemory> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameEventBusInMemory): Int =
    compareValues(instanceId, other.instanceId)
}
