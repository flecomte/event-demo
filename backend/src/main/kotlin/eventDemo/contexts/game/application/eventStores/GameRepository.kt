package eventDemo.contexts.game.application.eventStores

import eventDemo.contexts.game.domain.game.gameState.Game
import eventDemo.contexts.game.domain.game.gameState.GameCreated
import eventDemo.contexts.game.domain.game.gameState.GameInit
import eventDemo.libs.eventSource.eventStore.VersionConflictException
import eventDemo.shared.ids.GameId

interface GameRepository {
  fun get(id: GameId): Game?

  @Throws(VersionConflictException::class)
  fun save(game: Game)

  fun getOrCreate(gameId: GameId): Game =
    get(gameId) ?: create(gameId)

  fun create(gameId: GameId = GameId()): GameCreated =
    GameInit.createNewGame(gameId).also { save(it) }
}
