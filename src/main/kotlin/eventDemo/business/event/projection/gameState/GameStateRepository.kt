package eventDemo.business.event.projection

import eventDemo.business.entity.GameId

interface GameStateRepository {
  fun get(gameId: GameId): GameState
}
