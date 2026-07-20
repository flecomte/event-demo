package eventDemo.domain.event

import eventDemo.domain.entity.GameId
import eventDemo.domain.event.event.GameEvent
import eventDemo.libs.event.EventStore

interface GameEventStore : EventStore<GameEvent, GameId>
