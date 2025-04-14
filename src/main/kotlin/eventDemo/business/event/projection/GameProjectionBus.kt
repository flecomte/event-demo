package eventDemo.business.event.projection

import eventDemo.libs.bus.Bus

interface GameProjectionBus : Bus<GameProjection>
