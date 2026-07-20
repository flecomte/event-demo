package eventDemo.domain.event.projection

interface GameListRepository {
  fun getList(): List<GameList>
}
