package eventDemo.adapter.interfaceLayer

import eventDemo.business.entity.GameId
import eventDemo.business.event.projection.GameStateRepository
import eventDemo.configuration.GameIdSerializer
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.Serializable

@Serializable
@Resource("/game/{id}")
class Game(
  @Serializable(with = GameIdSerializer::class)
  val id: GameId,
) {
  @Serializable
  @Resource("card/last")
  class Card(
    val game: Game,
  )

  @Serializable
  @Resource("state")
  class State(
    val game: Game,
  )
}

/**
 * API routes to read the game state.
 */
fun Route.readTheGameState(gameStateRepository: GameStateRepository) {
  authenticate {
    // Read the last played card on the game.
    get<Game.Card> { body ->
      gameStateRepository
        .getLast(body.game.id)
        .cardOnCurrentStack
        ?.card
        ?.let { call.respond(it) }
        ?: call.response.status(HttpStatusCode.BadRequest)
    }

    // Read the last played card on the game.
    get<Game.State> { body ->
      val state = gameStateRepository.getLast(body.game.id)
      call.respond(state)
    }
  }
}
