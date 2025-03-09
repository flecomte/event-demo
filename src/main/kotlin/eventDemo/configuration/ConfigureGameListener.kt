package eventDemo.configuration

import eventDemo.app.eventListener.GameEventReactionListener
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configureGameListener() {
    GameEventReactionListener(get(), get(), get())
        .init()
}
