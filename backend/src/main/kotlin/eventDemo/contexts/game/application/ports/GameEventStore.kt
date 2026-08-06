package eventDemo.contexts.game.application.ports

import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.libs.eventSource.eventStore.EventStore
import eventDemo.shared.ids.GameId

interface GameEventStore : EventStore<GameEvent, GameId>
