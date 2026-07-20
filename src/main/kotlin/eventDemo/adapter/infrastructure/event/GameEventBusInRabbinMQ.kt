package eventDemo.adapter.infrastructure.event

import com.rabbitmq.client.ConnectionFactory
import eventDemo.domain.event.GameEventBus
import eventDemo.domain.event.event.GameEvent
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
