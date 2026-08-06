package eventDemo.contexts.game.infrastructure.persistence.eventBus

import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.events.GameEvent
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
