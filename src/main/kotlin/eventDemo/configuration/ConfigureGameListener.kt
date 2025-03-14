package eventDemo.configuration

import eventDemo.app.eventListener.ReactionEventListener
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configureGameListener() {
  ReactionEventListener(get(), get(), get())
    .init()
}
