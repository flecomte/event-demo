package eventDemo.adapter.interfaceLayer.query

import eventDemo.business.event.projection.GameListRepository
import io.ktor.resources.Resource
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.Serializable

@Serializable
@Resource("/games")
class Games

/**
 * API routes to show all games.
 */
fun Route.readGamesList(gameListRepository: GameListRepository) {
  authenticate {
    // Read the last played card on the game.
    get<Games> {
      val gameList = gameListRepository.getList()
      call.respond(gameList)
    }
  }
}
