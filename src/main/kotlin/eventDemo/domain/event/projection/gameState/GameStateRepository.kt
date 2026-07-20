package eventDemo.domain.event.projection

import eventDemo.domain.entity.GameId

interface GameStateRepository {
  fun get(gameId: GameId): GameState
}
