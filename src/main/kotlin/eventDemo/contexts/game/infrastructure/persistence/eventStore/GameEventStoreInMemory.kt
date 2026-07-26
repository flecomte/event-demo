package eventDemo.contexts.game.infrastructure.persistence.eventStore

import eventDemo.contexts.game.application.ports.GameEventStore
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.libs.eventSource.eventStore.EventStore
import eventDemo.libs.eventSource.eventStore.EventStoreInMemory

/**
 * A stream to publish and read the played card event.
 */
class GameEventStoreInMemory :
  GameEventStore,
  EventStore<GameEvent, GameId> by EventStoreInMemory()
