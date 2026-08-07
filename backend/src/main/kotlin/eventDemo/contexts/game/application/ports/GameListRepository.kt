package eventDemo.domain.event.projection

import eventDemo.shared.game.projection.GameList

interface GameListRepository {
  fun getList(
    limit: Int = 100,
    offset: Int = 0,
  ): List<GameList>

  fun save(gameList: GameList)

  fun subscribeToBus()
}
