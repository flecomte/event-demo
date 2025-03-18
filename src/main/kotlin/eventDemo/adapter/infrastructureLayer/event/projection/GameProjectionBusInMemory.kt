package eventDemo.adapter.infrastructureLayer.event.projection

import eventDemo.business.entity.GameId
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInMemory
import eventDemo.libs.event.projection.Projection
import java.util.UUID

class GameProjectionBusInMemory :
  GameProjectionBus,
  Bus<Projection<GameId>> by BusInMemory(),
  Comparable<GameProjectionBusInMemory> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameProjectionBusInMemory): Int =
    compareValues(instanceId, other.instanceId)
}
