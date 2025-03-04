package eventDemo.app.eventListener

import eventDemo.app.entity.GameId
import eventDemo.app.event.event.GameEvent
import eventDemo.libs.event.EventBus
import eventDemo.shared.toFrame
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

class GameEventPlayerNotificationListener(
    private val eventBus: EventBus<GameEvent, GameId>,
    private val outgoing: SendChannel<Frame>,
) {
    fun init() {
        eventBus.subscribe { event: GameEvent ->
            runBlocking {
                outgoing.send(event.toFrame())
            }
        }
    }
}
