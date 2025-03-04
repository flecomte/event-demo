package eventDemo.app.actions

import eventDemo.libs.event.EventBus
import eventDemo.shared.GameId
import eventDemo.shared.event.GameEvent
import eventDemo.shared.toFrame
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

class GameEventPlayerNotificationSubscriber(
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
