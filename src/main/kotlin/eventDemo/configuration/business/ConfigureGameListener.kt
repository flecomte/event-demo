package eventDemo.configuration.business

import eventDemo.business.event.projection.projectionListener.ReactionListener
import io.ktor.server.application.Application
import org.koin.ktor.ext.get

fun Application.configureGameListener() {
  ReactionListener(get(), get())
    .init()
}
