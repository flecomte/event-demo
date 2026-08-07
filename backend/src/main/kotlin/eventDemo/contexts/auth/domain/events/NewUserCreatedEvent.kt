package eventDemo.contexts.auth.domain.events

import eventDemo.shared.ids.EventId
import eventDemo.shared.ids.UserId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class NewUserCreatedEvent(
  val username: String,
  val password: String,
  override val version: Int,
  override val createdAt: Instant = Clock.System.now(),
  override val aggregateId: UserId = UserId(),
) : UserEvent {
  override val eventId: EventId = EventId()
}
