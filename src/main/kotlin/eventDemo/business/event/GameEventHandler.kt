package eventDemo.business.event

import eventDemo.business.entity.GameId
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.event.EventHandler
import eventDemo.libs.event.EventHandlerImpl
import eventDemo.libs.event.VersionBuilder

/**
 * Handle the event to dispatch it to store, bus and projections builders
 */
class GameEventHandler(
  private val eventBus: GameEventBus,
  private val eventStore: GameEventStore,
  private val versionBuilder: VersionBuilder,
) : EventHandler<GameEvent, GameId> by EventHandlerImpl(eventBus, eventStore, versionBuilder)
