package eventDemo.adapter.infrastructureLayer.event

import eventDemo.business.entity.GameId
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.event.EventBus
import eventDemo.libs.event.EventBusInMemory

class GameEventBusInMemory :
  GameEventBus,
  EventBus<GameEvent, GameId> by EventBusInMemory<GameEvent, GameId>()
