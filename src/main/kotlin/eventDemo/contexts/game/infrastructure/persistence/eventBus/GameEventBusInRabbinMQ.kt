package eventDemo.contexts.game.infrastructure.persistence.eventBus

import com.rabbitmq.client.ConnectionFactory
import eventDemo.contexts.game.application.ports.GameEventBus
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInRabbitMQ
import kotlinx.serialization.json.Json
import java.util.UUID

class GameEventBusInRabbinMQ(
  private val connectionFactory: ConnectionFactory,
) : GameEventBus,
  Bus<GameEvent> by BusInRabbitMQ(
    connectionFactory,
    "GameEvent",
    { Json.encodeToString(it) },
    { Json.decodeFromString<GameEvent>(it) },
  ),
  Comparable<GameEventBusInRabbinMQ> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameEventBusInRabbinMQ): Int =
    compareValues(instanceId, other.instanceId)
}
