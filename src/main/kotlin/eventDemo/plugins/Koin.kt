package eventDemo.plugins

import eventDemo.app.actions.playNewCard.PlayCardCommandHandler
import eventDemo.shared.command.GameCommandStream
import eventDemo.shared.event.GameEventStream
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

val appModule =
    module {
        singleOf<GameEventStream>(::GameEventStream)
        singleOf<GameCommandStream>(::GameCommandStream)
        singleOf(::PlayCardCommandHandler)
    }
