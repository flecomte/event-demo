package eventDemo.app.actions

import eventDemo.app.Card
import eventDemo.app.EventStream
import eventDemo.app.GameId
import eventDemo.app.PlayCardEvent
import eventDemo.app.read
import eventDemo.plugins.GameIdSerializer
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
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
    @Resource("card")
    class Card(val game: Game) {
        @Serializable
        @Resource("")
        class PutCard(val card: Card)

        @Serializable
        @Resource("last")
        class LastCard(val card: Card)
    }
}

fun Routing.card() {
    val eventStream by inject<EventStream<GameId>>()

    post<Game.Card.PutCard> {
        val card = call.receive<Card>()
        eventStream.publish(PlayCardEvent(it.card.game.id, card))
        call.respondNullable<Any?>(HttpStatusCode.OK, null)
    }

    get<Game.Card.LastCard> {
        eventStream.read<PlayCardEvent, GameId>(it.card.game.id)
            ?.let { it1 -> call.respond<Card>(it1.card) }
            ?: call.response.status(HttpStatusCode.BadRequest)
    }
}
