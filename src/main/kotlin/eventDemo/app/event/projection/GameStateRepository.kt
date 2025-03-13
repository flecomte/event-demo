package eventDemo.app.event.projection

import eventDemo.app.entity.GameId
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.GameEvent

class GameStateRepository(
    eventStream: GameEventStream,
    eventHandler: GameEventHandler,
    snapshotConfig: SnapshotConfig = SnapshotConfig(),
) {
    private val projectionsSnapshot =
        ProjectionSnapshotRepositoryInMemory(
            eventStream = eventStream,
            snapshotCacheConfig = snapshotConfig,
            applyToProjection = GameState::apply,
            initialStateBuilder = { aggregateId: GameId -> GameState(aggregateId) },
        )

    init {
        eventHandler.registerProjectionBuilder { event ->
            projectionsSnapshot.applyAndPutToCache(event)
        }
    }

    /**
     * Get the last version of the [GameState] from the all eventStream.
     *
     * It fetches it from the local cache if possible, otherwise it builds it.
     */
    fun getLast(gameId: GameId): GameState = projectionsSnapshot.getLast(gameId)

    /**
     * Get the [GameState] to the specific [event][GameEvent].
     * It does not contain the [events][GameEvent] it after this one.
     *
     * It fetches it from the local cache if possible, otherwise it builds it.
     */
    fun getUntil(event: GameEvent): GameState = projectionsSnapshot.getUntil(event)
}
