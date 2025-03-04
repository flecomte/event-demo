package eventDemo.app.actions

import eventDemo.app.GameId
import eventDemo.app.event.CardIsPlayedEvent
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.buildStateFromEventStream
import eventDemo.configuration.GameIdSerializer
import eventDemo.libs.event.readLastOf
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

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
 * API route to read the last card played.
 */
fun Routing.readLastPlayedCard() {
    val eventStream by inject<GameEventStream>()

    /*
     * Read the last played card on the game.
     */
    get<Game.Card> { body ->
        eventStream
            .readLastOf<CardIsPlayedEvent, _, _>(body.game.id)
            ?.let { call.respond(it.card) }
            ?: call.response.status(HttpStatusCode.BadRequest)
    }
}

/**
 * API route to read the last card played.
 */
fun Routing.readGameState() {
    val eventStream by inject<GameEventStream>()

    /*
     * Read the last played card on the game.
     */
    get<Game.State> { body ->
        val state = body.game.id.buildStateFromEventStream(eventStream)
        call.respond(state)
    }
}
