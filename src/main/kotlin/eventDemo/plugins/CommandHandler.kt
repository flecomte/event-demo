package eventDemo.plugins

import eventDemo.app.actions.playNewCard.PlayCardCommandHandler
import io.ktor.server.application.Application
import org.koin.java.KoinJavaComponent.getKoin

fun Application.configureCommandHandler() {
    getKoin().get<PlayCardCommandHandler>()()
}
