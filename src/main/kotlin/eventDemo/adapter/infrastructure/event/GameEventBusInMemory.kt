package eventDemo.adapter.infrastructure.event

import eventDemo.domain.event.GameEventBus
import eventDemo.domain.event.event.GameEvent
import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInMemory
import java.util.UUID

class GameEventBusInMemory :
  GameEventBus,
  Bus<GameEvent> by BusInMemory(GameEventBusInMemory::class),
  Comparable<GameEventBusInMemory> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameEventBusInMemory): Int =
    compareValues(instanceId, other.instanceId)
}
