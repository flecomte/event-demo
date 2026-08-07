package eventDemo.contexts.game.application.eventStores

import eventDemo.contexts.game.application.ports.GameEventStore
import eventDemo.contexts.game.domain.game.gameState.Game
import eventDemo.libs.eventSource.eventStore.VersionConflictException
import eventDemo.shared.ids.GameId

class GameEventStoreRepository(
  val eventStore: GameEventStore,
) : GameRepository {
  override fun get(id: GameId): Game? {
    val events =
      eventStore
        .getStream(id)
        .readAll()
    if (events.isEmpty()) {
      return null
    }
    return events.let { Game.loadFromHistory(it) }
  }

  @Throws(VersionConflictException::class)
  override fun save(game: Game) {
    eventStore.append(game.recordedEvents)
  }
}
