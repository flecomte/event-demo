package eventDemo.adapter.infrastructureLayer.event

import eventDemo.business.event.GameEventBus
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.bus.Bus
import eventDemo.libs.bus.BusInMemory

class GameEventBusInMemory :
  GameEventBus(),
  Bus<GameEvent> by BusInMemory()
