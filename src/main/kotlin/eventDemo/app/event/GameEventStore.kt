package eventDemo.app.event

import eventDemo.app.entity.GameId
import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.EventStore

/**
 * A stream to publish and read the played card event.
 */
class GameEventStore(
  private val eventStore: EventStore<GameEvent, GameId>,
) : EventStore<GameEvent, GameId> by eventStore
