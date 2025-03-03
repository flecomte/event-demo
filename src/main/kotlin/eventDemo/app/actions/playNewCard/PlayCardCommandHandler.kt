package eventDemo.app.actions.playNewCard

import eventDemo.shared.command.GameCommandStream
import eventDemo.shared.event.CardIsPlayedEvent
import eventDemo.shared.event.GameEventStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listen [PlayCardCommand] on [GameCommandStream], check the validity and execute the action.
 *
 * This action produces a new [CardIsPlayedEvent]
 */
class PlayCardCommandHandler(
    private val commandStream: GameCommandStream,
    private val eventStream: GameEventStream,
) {
    /**
     * Init the handler
     */
    fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            commandStream.process {
                // TODO check the command can be executed
                eventStream.publish(CardIsPlayedEvent(it.payload.game.id, it.payload.card))
            }
        }
    }
}
