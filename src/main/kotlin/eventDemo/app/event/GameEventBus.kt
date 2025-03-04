package eventDemo.app.event

import eventDemo.app.entity.GameId
import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.EventBus

class GameEventBus(
    bus: EventBus<GameEvent, GameId>,
) : EventBus<GameEvent, GameId> by bus
