package eventDemo.contexts.game.infrastructure.persistence.eventStore

import eventDemo.contexts.game.application.ports.GameEventStore
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.libs.eventSource.eventStore.EventStore
import eventDemo.libs.eventSource.eventStore.EventStoreInPostgresql
import kotlinx.serialization.json.Json
import javax.sql.DataSource

/**
 * A stream to publish and read the played card event.
 */
class GameEventStoreInPostgresql(
  dataSource: DataSource,
) : GameEventStore,
  EventStore<GameEvent, GameId> by EventStoreInPostgresql(
    dataSource,
    { Json.encodeToString(it) },
    { Json.decodeFromString(it) },
    "game.game_event_stream",
  )
