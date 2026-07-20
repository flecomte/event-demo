package eventDemo.domain.event

import eventDemo.domain.event.event.GameEvent
import eventDemo.libs.bus.Bus

interface GameEventBus : Bus<GameEvent>
