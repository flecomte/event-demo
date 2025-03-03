package eventDemo.shared.event

import eventDemo.libs.event.EventStreamInMemory
import eventDemo.shared.GameId

/**
 * A stream to publish and read the played card event.
 */
class GameEventStream : EventStreamInMemory<GameEvent, GameId>(GameEvent::class.java)
