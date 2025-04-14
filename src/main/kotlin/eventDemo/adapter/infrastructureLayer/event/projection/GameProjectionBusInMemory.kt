package eventDemo.adapter.infrastructureLayer.event.projection

import eventDemo.business.event.projection.GameProjection
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInMemory
import java.util.UUID

class GameProjectionBusInMemory :
  GameProjectionBus,
  Bus<GameProjection> by BusInMemory(GameProjectionBusInMemory::class),
  Comparable<GameProjectionBusInMemory> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameProjectionBusInMemory): Int =
    compareValues(instanceId, other.instanceId)
}
