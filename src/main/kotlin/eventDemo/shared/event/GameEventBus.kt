package eventDemo.shared.event

import eventDemo.libs.event.EventBus
import eventDemo.shared.GameId

class GameEventBus(
    bus: EventBus<GameEvent, GameId>,
) : EventBus<GameEvent, GameId> by bus
