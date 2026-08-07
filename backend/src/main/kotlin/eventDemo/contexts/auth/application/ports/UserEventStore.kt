package eventDemo.contexts.auth.application.ports

import eventDemo.contexts.auth.domain.events.UserEvent
import eventDemo.libs.eventSource.eventStore.EventStore
import eventDemo.shared.ids.UserId

interface UserEventStore : EventStore<UserEvent, UserId>
