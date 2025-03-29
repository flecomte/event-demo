package eventDemo.libs.event

import eventDemo.configuration.serializer.UUIDSerializer
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
  @Serializable(with = UUIDSerializer::class)
  override val eventId: UUID = UUID.randomUUID(),
  override val aggregateId: IdTest,
  override val createdAt: Instant = Clock.System.now(),
  override val version: Int,
  val num: Int,
) : TestEvents
