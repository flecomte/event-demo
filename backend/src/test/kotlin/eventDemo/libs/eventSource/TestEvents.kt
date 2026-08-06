package eventDemo.libs.eventSource

import eventDemo.shared.ids.AggregateId
import eventDemo.shared.ids.EventId
import eventDemo.shared.serializers.EventIdSerializer
import eventDemo.shared.serializers.UUIDSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class IdTest(
  @Serializable(with = UUIDSerializer::class)
  override val id: Uuid = Uuid.random(),
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
