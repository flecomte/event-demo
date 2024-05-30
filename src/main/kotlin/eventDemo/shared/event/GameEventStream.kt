package eventDemo.shared.event

import eventDemo.libs.event.EventStreamInMemory
import eventDemo.shared.GameId

class GameEventStream : EventStreamInMemory<GameEvent, GameId>(GameEvent::class.java)
