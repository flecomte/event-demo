package eventDemo.app.eventListener

import eventDemo.app.event.GameEventBus
import eventDemo.app.event.event.GameEvent
import eventDemo.shared.toFrame
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking

class GameEventPlayerNotificationListener(
    private val eventBus: GameEventBus,
) {
    fun startListening(outgoing: SendChannel<Frame>) {
        eventBus.subscribe { event: GameEvent ->
            outgoing.trySendBlocking(event.toFrame())
        }
    }
}
