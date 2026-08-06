package eventDemo.contexts.game.application.ports

import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.libs.bus.Bus

interface GameEventBus : Bus<GameEvent>
