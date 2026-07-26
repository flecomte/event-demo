package eventDemo.contexts.game.application.ports

import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.libs.eventSource.eventStore.EventStore

interface GameEventStore : EventStore<GameEvent, GameId>
