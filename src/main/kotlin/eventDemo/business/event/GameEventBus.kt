package eventDemo.business.event

import eventDemo.business.event.event.GameEvent
import eventDemo.libs.bus.Bus

interface GameEventBus : Bus<GameEvent>
