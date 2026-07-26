package eventDemo.contexts.game.application.ports

import eventDemo.contexts.game.infrastructure.persistence.projections.models.GameProjection
import eventDemo.libs.bus.Bus

interface GameProjectionBus : Bus<GameProjection>
