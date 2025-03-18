package eventDemo.business.event.projection

import eventDemo.business.entity.GameId
import eventDemo.libs.bus.Bus
import eventDemo.libs.event.projection.Projection

interface GameProjectionBus : Bus<Projection<GameId>>
