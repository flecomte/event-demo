package eventDemo.app.actions.playNewCard

import eventDemo.libs.command.send
import eventDemo.shared.GameId
import eventDemo.shared.command.GameCommandStreamInMemory
import eventDemo.shared.entity.Card
import eventDemo.shared.entity.Player
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.post
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.Routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
@Resource("/game/{id}")
class GameRoute(
//    @Serializable(with = GameIdSerializer::class)
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
    val commandStream = GameCommandStreamInMemory()
    authenticate {
        /*
         * A player request to play a new card.
         *
         * It always returns [HttpStatusCode.OK], but it is not mean that card is already played!
         */
        post<GameRoute.Card> {
            val card = call.receive<Card>()
            val name = call.principal<Player>()!!
            launch(Dispatchers.Default) {
                commandStream.send(
                    PlayCardCommand(
                        it.game.id,
                        name,
                        card,
                    ),
                )
            }

            call.respondNullable<Any?>(HttpStatusCode.OK, null)
        }
    }
}
