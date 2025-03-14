package eventDemo.libs.event

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class VersionBuilderLocal : VersionBuilder {
  private val logger = KotlinLogging.logger { }
  private val versions: ConcurrentHashMap<AggregateId, AtomicInteger> = ConcurrentHashMap()

  override fun buildNextVersion(aggregateId: AggregateId): Int =
    versionOfAggregate(aggregateId)
      .addAndGet(1)
      .also { logger.debug { "New version $it" } }

  override fun getLastVersion(aggregateId: AggregateId): Int =
    versionOfAggregate(aggregateId).toInt()

  private fun versionOfAggregate(aggregateId: AggregateId) =
    versions
      .computeIfAbsent(aggregateId) { AtomicInteger(0) }
}
