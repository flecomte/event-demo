package eventDemo.adapter.infrastructureLayer.event.projection

import com.rabbitmq.client.ConnectionFactory
import eventDemo.business.event.projection.GameProjection
import eventDemo.business.event.projection.GameProjectionBus
import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInRabbitMQ
import kotlinx.serialization.json.Json
import java.util.UUID

class GameProjectionBusInRabbitMQ(
  private val connectionFactory: ConnectionFactory,
) : GameProjectionBus,
  Bus<GameProjection> by BusInRabbitMQ(
    connectionFactory,
    "GameProjection",
    { Json.encodeToString(it) },
    { Json.decodeFromString<GameProjection>(it) },
  ),
  Comparable<GameProjectionBusInRabbitMQ> {
  private val instanceId: UUID = UUID.randomUUID()

  override fun compareTo(other: GameProjectionBusInRabbitMQ): Int =
    compareValues(instanceId, other.instanceId)
}
