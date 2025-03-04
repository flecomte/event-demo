package eventDemo.app.query

import eventDemo.app.entity.GameId
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.buildStateFromEventStream
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.libs.event.readLastOf
import eventDemo.shared.GameIdSerializer
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
fun Route.readTheGameState(eventStream: GameEventStream) {
    authenticate {
        // Read the last played card on the game.
        get<Game.Card> { body ->
            eventStream
                .readLastOf<CardIsPlayedEvent, _, _>(body.game.id)
                ?.let { call.respond(it.card) }
                ?: call.response.status(HttpStatusCode.BadRequest)
        }

        // Read the last played card on the game.
        get<Game.State> { body ->
            val state = body.game.id.buildStateFromEventStream(eventStream)
            call.respond(state)
        }
    }
}
