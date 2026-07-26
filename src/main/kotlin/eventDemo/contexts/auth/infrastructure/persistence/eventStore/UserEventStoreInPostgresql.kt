package eventDemo.contexts.auth.infrastructure.persistence.eventStore

import eventDemo.contexts.auth.application.ports.UserEventStore
import eventDemo.contexts.auth.domain.events.UserEvent
import eventDemo.libs.eventSource.eventStore.EventStore
import eventDemo.libs.eventSource.eventStore.EventStoreInPostgresql
import eventDemo.sharedKernel.UserId
import kotlinx.serialization.json.Json
import javax.sql.DataSource

/**
 * A stream to publish and read the user events.
 */
class UserEventStoreInPostgresql(
  dataSource: DataSource,
) : UserEventStore,
  EventStore<UserEvent, UserId> by EventStoreInPostgresql(
    dataSource,
    { Json.encodeToString(it) },
    { Json.decodeFromString(it) },
    "auth.user_event_stream",
  )
