package eventDemo.adapter.infrastructureLayer.event

import com.rabbitmq.client.ConnectionFactory
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.event.GameEvent
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
