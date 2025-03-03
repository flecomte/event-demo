package eventDemo

import eventDemo.app.actions.playNewCard.PlayCardCommandHandler
import io.ktor.server.application.Application
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Configure the command handler for the PlayCard.
 */
fun Application.configureCommandHandler() {
    getKoin()
        .get<PlayCardCommandHandler>()
        .init()
}
