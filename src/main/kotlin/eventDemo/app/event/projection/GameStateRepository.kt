package eventDemo.app.event.projection

import eventDemo.app.entity.GameId
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.GameEventStream
import java.util.concurrent.ConcurrentHashMap

class GameStateRepository(
    private val eventStream: GameEventStream,
    eventHandler: GameEventHandler,
) {
    private val projections: ConcurrentHashMap<GameId, GameState> = ConcurrentHashMap()

    init {
        eventHandler.registerProjectionBuilder { event ->
            val projection = projections[event.gameId]
            if (projection == null) {
                event.gameId
                    .buildStateFromEventStream(eventStream)
                    .update()
            } else {
                projection
                    .apply(event)
                    .let { projections.put(it.gameId, it) }
            }
        }
    }

    fun get(gameId: GameId): GameState = gameId.buildStateFromEventStream(eventStream)

    private fun GameState.update() {
        projections[gameId] = this
    }
}
