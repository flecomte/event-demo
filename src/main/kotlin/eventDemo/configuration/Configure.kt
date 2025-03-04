package eventDemo.configuration

import eventDemo.app.eventListener.GameEventReactionListener
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configure() {
    configureKoin()

    configureSecurity()

    configureSerialization()

    configureWebSockets()
    declareWebSocketsGameRoute(get(), get())

    configureHttpRouting()
    declareHttpGameRoute()

    GameEventReactionListener(get(), get())
        .init()
}
