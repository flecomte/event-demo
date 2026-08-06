package eventDemo.contexts.game.infrastructure.rest

import eventDemo.contexts.game.application.eventStores.GameRepository
import eventDemo.contexts.game.application.notification.toNotification
import eventDemo.contexts.game.application.ports.GameEventStore
import eventDemo.shared.ids.GameId
import eventDemo.shared.serializers.GameIdSerializer
import eventDemo.sharedKernel.currentUserId
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.auth.authenticate
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.serialization.Serializable

@Serializable
@Resource("/games/{id}")
class Game(
  @Serializable(with = GameIdSerializer::class)
  val id: GameId,
)

/**
 * API routes to read the game state.
 */
fun Route.getFullNotificationsRoute(
  gameRepository: GameRepository,
  gameEventStore: GameEventStore,
) {
  authenticate {
    get<Game> { body ->
      val game =
        gameRepository.get(body.id)
          ?: return@get call.respond(HttpStatusCode.NotFound)
      val notifications =
        gameEventStore
          .getStream(body.id)
          .readAll()
          .flatMap { it.toNotification(game, call.currentUserId) }

      call.respond(notifications)
    }
  }
}
