package eventDemo.configuration

import eventDemo.app.GameEventReactionListener
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configure() {
    configureKoin()

    configureSecurity()

    configureSerialization()

    configureSockets()
    configureWebSocketsGameRoute(get(), get())

    configureHttp()
    configureHttpRouting()

    GameEventReactionListener(get(), get())
        .init()
}
