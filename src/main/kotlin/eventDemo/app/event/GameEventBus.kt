package eventDemo.app.event

import eventDemo.app.GameId
import eventDemo.libs.event.EventBus

class GameEventBus(
    bus: EventBus<GameEvent, GameId>,
) : EventBus<GameEvent, GameId> by bus
