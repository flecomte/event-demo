package eventDemo.business.event.projection.gameState

import eventDemo.business.entity.GameId
import eventDemo.business.event.event.GameEvent

interface GameStateRepository {
  fun getLast(gameId: GameId): GameState

  fun getUntil(event: GameEvent): GameState
}
