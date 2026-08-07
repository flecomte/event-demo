package eventDemo.contexts.auth.domain.events

import eventDemo.libs.eventSource.Event
import eventDemo.shared.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
sealed interface UserEvent : Event<UserId>
