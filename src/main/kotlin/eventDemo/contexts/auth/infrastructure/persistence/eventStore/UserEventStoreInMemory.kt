package eventDemo.contexts.auth.infrastructure.persistence.eventStore

import eventDemo.contexts.auth.application.ports.UserEventStore
import eventDemo.contexts.auth.domain.events.UserEvent
import eventDemo.libs.eventSource.eventStore.EventStore
import eventDemo.libs.eventSource.eventStore.EventStoreInMemory
import eventDemo.sharedKernel.UserId

/**
 * A stream to publish and read the user events.
 */
class UserEventStoreInMemory :
  UserEventStore,
  EventStore<UserEvent, UserId> by EventStoreInMemory()
