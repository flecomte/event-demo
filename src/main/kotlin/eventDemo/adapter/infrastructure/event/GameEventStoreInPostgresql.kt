package eventDemo.adapter.infrastructure.event

import eventDemo.domain.entity.GameId
import eventDemo.domain.event.GameEventStore
import eventDemo.domain.event.event.GameEvent
import eventDemo.libs.event.EventStore
import eventDemo.libs.event.EventStoreInPostgresql
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
  )
