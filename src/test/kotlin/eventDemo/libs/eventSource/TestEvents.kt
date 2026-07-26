package eventDemo.libs.eventSource

import eventDemo.contexts.game.infrastructure.persistence.serializers.EventIdSerializer
import eventDemo.libs.serializer.UUIDSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
@Serializable
value class IdTest(
  @Serializable(with = UUIDSerializer::class)
  override val id: UUID = UUID.randomUUID(),
) : AggregateId

@Serializable
sealed interface TestEvents : Event<IdTest>

@Serializable
data class EventXTest(
  @Serializable(with = EventIdSerializer::class)
  override val eventId: EventId = EventId(),
  override val aggregateId: IdTest,
  override val createdAt: Instant = Clock.System.now(),
  override val version: Int,
  val num: Int,
) : TestEvents
