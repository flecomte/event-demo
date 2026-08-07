package eventDemo.contexts.game.application.ports

import eventDemo.libs.bus.Bus
import eventDemo.shared.game.projection.GameProjection

interface GameProjectionBus : Bus<GameProjection>
