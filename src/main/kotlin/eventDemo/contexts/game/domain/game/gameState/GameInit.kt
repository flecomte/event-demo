package eventDemo.contexts.game.domain.game.gameState

import eventDemo.contexts.game.domain.events.GameCreatedEvent
import eventDemo.contexts.game.domain.events.GameEvent
import eventDemo.contexts.game.domain.game.GameId
import eventDemo.contexts.game.domain.game.PlayerList

data class GameInit(
  override val aggregateId: GameId,
) : Game {
  override val players: PlayerList = PlayerList()
  override var recordedEvents: Set<GameEvent> = emptySet()

  // 0 = no events; not persisted; not really exist.
  override var version: Int = 0

  companion object {
    fun createNewGame(gameId: GameId = GameId()): GameCreated {
      val event = GameCreatedEvent(gameId, 1)
      return GameInit(event.aggregateId).run {
        event.run(::applyEvent)
      }
    }
  }

  internal fun applyEvent(event: GameCreatedEvent): GameCreated =
    GameCreated(aggregateId, recordedEvents = setOf(event), version = event.version)
}
