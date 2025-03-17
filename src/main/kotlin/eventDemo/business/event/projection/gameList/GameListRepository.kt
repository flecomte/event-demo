package eventDemo.business.event.projection.gameList

interface GameListRepository {
  fun getList(): List<GameList>
}
