package eventDemo.adapter.infrastructureLayer.event

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.event.EventStore
import eventDemo.libs.event.EventStoreInMemory

/**
 * A stream to publish and read the played card event.
 */
class GameEventStoreInMemory :
  GameEventStore,
  EventStore<GameEvent, GameId> by EventStoreInMemory()
