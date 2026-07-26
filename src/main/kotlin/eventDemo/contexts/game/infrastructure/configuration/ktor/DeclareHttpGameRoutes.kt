package eventDemo.contexts.game.infrastructure.configuration.ktor

import eventDemo.contexts.game.infrastructure.rest.gamesListRoute
import eventDemo.contexts.game.infrastructure.rest.getFullNotificationsRoute
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get as getDi

fun Application.declareHttpGameRoute() {
  routing {
    gamesListRoute(getDi())
    getFullNotificationsRoute(getDi(), getDi())
  }
}
