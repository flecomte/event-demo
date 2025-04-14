package eventDemo.business.event.projection

interface GameListRepository {
  fun getList(): List<GameList>
}
