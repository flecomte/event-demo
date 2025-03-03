package eventDemo.app.actions.playNewCard

import eventDemo.libs.command.send
import eventDemo.plugins.GameIdSerializer
import eventDemo.shared.GameId
import eventDemo.shared.command.GameCommandStream
import eventDemo.shared.entity.Card
import eventDemo.shared.entity.Game
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
@Resource("/game/{id}")
class GameRoute(
    @Serializable(with = GameIdSerializer::class)
    val id: GameId,
) {
    @Serializable
    @Resource("card")
    class Card(
        val game: GameRoute,
    )
}

/**
 * API route to send a request to play card.
 */
fun Routing.playNewCard() {
    val commandStream by inject<GameCommandStream>()

    /*
     * A player request to play a new card.
     *
     * It always returns [HttpStatusCode.OK], but it is not mean that card is already played!
     */
    post<GameRoute.Card> {
        val card = call.receive<Card>()
        launch(Dispatchers.Default) {
            commandStream.send(PlayCardCommand(Game(it.game.id), card))
        }

        call.respondNullable<Any?>(HttpStatusCode.OK, null)
    }
}
