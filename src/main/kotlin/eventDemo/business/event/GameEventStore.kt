package eventDemo.business.event

import eventDemo.business.entity.GameId
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.event.EventStore

interface GameEventStore : EventStore<GameEvent, GameId>
