package eventDemo.contexts.game.infrastructure.persistence.projections.bus

import eventDemo.contexts.game.application.ports.GameProjectionBus
import eventDemo.contexts.game.infrastructure.persistence.projections.models.GameProjection
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
