package eventDemo.app.event.projection

import eventDemo.app.entity.GameId
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.GameEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class GameStateRepository(
    private val eventStream: GameEventStream,
    eventHandler: GameEventHandler,
    private val maxSnapshotCacheSize: Int = 20,
) {
    private val projections: ConcurrentHashMap<GameId, GameState> = ConcurrentHashMap()
    private val version: AtomicInteger = AtomicInteger(0)
    private val projectionsSnapshot: ConcurrentHashMap<GameEvent, GameState> = ConcurrentHashMap()
    private val sortedSnapshotByVersion: ConcurrentHashMap<GameEvent, Int> = ConcurrentHashMap()

    init {
        eventHandler.registerProjectionBuilder { event ->
            val projection = projections[event.gameId]
            if (projection == null) {
                event
                    .buildStateFromEventStreamTo(eventStream)
                    .update()
            } else {
                projection
                    .apply(event)
                    .also { projections[it.gameId] = it }
                    .also { state ->
                        val newVersion = version.addAndGet(1)
                        saveSnapshot(event, state, newVersion)
                        removeOldSnapshot()
                    }
            }
        }
    }

    private fun removeOldSnapshot() {
        if (projectionsSnapshot.size > maxSnapshotCacheSize) {
            val numberToRemove = projectionsSnapshot.size - maxSnapshotCacheSize
            sortedSnapshotByVersion
                .toList()
                .sortedBy { it.second }
                .take(numberToRemove)
                .toMap()
                .keys
                .forEach { event ->
                    sortedSnapshotByVersion.remove(event)
                    projectionsSnapshot.remove(event)
                }
        }
    }

    private fun saveSnapshot(
        event: GameEvent,
        state: GameState,
        newVersion: Int,
    ) {
        projectionsSnapshot[event] = state
        sortedSnapshotByVersion[event] = newVersion
    }

    /**
     * Get the last version of the [GameState] from the all eventStream.
     *
     * It fetches it from the local cache if possible, otherwise it builds it.
     */
    fun get(gameId: GameId): GameState =
        projections.computeIfAbsent(gameId) {
            gameId.buildStateFromEventStream(eventStream)
        }

    /**
     * Get the [GameState] to the specific [event][GameEvent].
     * It does not contain the [events][GameEvent] it after this one.
     *
     * It fetches it from the local cache if possible, otherwise it builds it.
     */
    fun getUntil(event: GameEvent): GameState =
        projectionsSnapshot.computeIfAbsent(event) {
            event.buildStateFromEventStreamTo(eventStream)
        }

    private fun GameState.update() {
        projections[gameId] = this
    }

    /**
     * Build the state to the specific event
     */
    private fun GameEvent.buildStateFromEventStreamTo(eventStream: GameEventStream): GameState =
        run { eventStream.readAll(gameId).takeWhile { it != this } + this }.buildStateFromEvents()
}
