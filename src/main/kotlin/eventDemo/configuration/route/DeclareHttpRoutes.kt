package eventDemo.configuration.route

import eventDemo.adapter.presenter.query.readGamesList
import eventDemo.adapter.presenter.query.readTheGameState
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get

fun Application.declareHttpGameRoute() {
  routing {
    readTheGameState(this@declareHttpGameRoute.get())
    readGamesList(this@declareHttpGameRoute.get())
  }
}
