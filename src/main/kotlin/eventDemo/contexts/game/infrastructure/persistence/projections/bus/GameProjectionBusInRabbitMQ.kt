package eventDemo.contexts.game.infrastructure.persistence.projections.bus

import com.rabbitmq.client.ConnectionFactory
import eventDemo.contexts.game.application.ports.GameProjectionBus
import eventDemo.contexts.game.infrastructure.persistence.projections.models.GameProjection
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
