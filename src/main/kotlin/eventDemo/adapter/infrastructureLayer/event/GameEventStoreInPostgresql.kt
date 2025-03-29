package eventDemo.adapter.infrastructureLayer.event

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.event.GameEvent
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
