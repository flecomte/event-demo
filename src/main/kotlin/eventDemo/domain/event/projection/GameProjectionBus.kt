package eventDemo.domain.event.projection

import eventDemo.libs.bus.Bus

interface GameProjectionBus : Bus<GameProjection>
