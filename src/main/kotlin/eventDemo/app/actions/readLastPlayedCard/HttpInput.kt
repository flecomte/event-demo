package eventDemo.app.actions.readLastPlayedCard

import eventDemo.libs.event.readLastOf
import eventDemo.plugins.GameIdSerializer
import eventDemo.shared.GameId
import eventDemo.shared.event.CardIsPlayedEvent
import eventDemo.shared.event.GameEventStream
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
}

/**
 * API route to read the last card played.
 */
fun Routing.readLastPlayedCard() {
    val eventStream by inject<GameEventStream>()

    /*
     * Read the last played card on the game.
     */
    get<Game.Card> { card ->
        eventStream
            .readLastOf<CardIsPlayedEvent, _, _>(card.game.id)
            ?.let { call.respond(it.card) }
            ?: call.response.status(HttpStatusCode.BadRequest)
    }
}
