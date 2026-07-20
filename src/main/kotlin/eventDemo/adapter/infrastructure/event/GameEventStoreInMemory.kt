package eventDemo.adapter.infrastructure.event

import eventDemo.domain.entity.GameId
import eventDemo.domain.event.GameEventStore
import eventDemo.domain.event.event.GameEvent
import eventDemo.libs.event.EventStore
import eventDemo.libs.event.EventStoreInMemory

/**
 * A stream to publish and read the played card event.
 */
class GameEventStoreInMemory :
  GameEventStore,
  EventStore<GameEvent, GameId> by EventStoreInMemory()
