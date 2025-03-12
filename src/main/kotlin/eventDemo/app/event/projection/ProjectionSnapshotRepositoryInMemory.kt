package eventDemo.app.event.projection

import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import java.util.concurrent.ConcurrentHashMap

class ProjectionSnapshotRepositoryInMemory<E : Event<ID>, P : Projection<ID>, ID : AggregateId>(
    private val maxSnapshotCacheSize: Int = 20,
    private val applyToProjection: P?.(event: E) -> P,
) {
    private val projectionsSnapshot: ConcurrentHashMap<E, P> = ConcurrentHashMap()

    fun applyAndPutToCache(event: E): P {
        // lock here
        return projectionsSnapshot
            .filterKeys { it.aggregateId == event.aggregateId }
            .toList()
            .find { (e, _) -> e.version == (event.version - 1) }
            ?.second
            .applyToProjection(event)
            .also { projectionsSnapshot.put(event, it) }
            .also { removeOldSnapshot() }
        // Unlock here
    }

    private fun removeOldSnapshot() {
        if (projectionsSnapshot.size > maxSnapshotCacheSize) {
            val numberToRemove = projectionsSnapshot.size - maxSnapshotCacheSize

            projectionsSnapshot
                .keys
                .sortedBy { it.version }
                .take(numberToRemove)
                .forEach { event ->
                    projectionsSnapshot.remove(event)
                }
        }
    }

    /**
     * Get the last version of the [Projection] from the cache.
     */
    fun getLast(aggregateId: ID): P? =
        projectionsSnapshot
            .filter { it.key.aggregateId == aggregateId }
            .maxByOrNull { (event, _) -> event.version }
            ?.value

    /**
     * Get the [Projection] to the specific [event][Event].
     * It does not contain the [events][Event] it after this one.
     */
    fun getUntil(event: E): P? = projectionsSnapshot.get(event)
}
