package eventDemo.libs.event

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class VersionBuilderLocal : VersionBuilder {
  private val logger = KotlinLogging.logger { }
  private val versions: ConcurrentHashMap<AggregateId, AtomicInteger> = ConcurrentHashMap()

  override fun buildNextVersion(aggregateId: AggregateId): Int =
    withLoggingContext("aggregateId" to aggregateId.toString()) {
      versionOfAggregate(aggregateId)
        .addAndGet(1)
        .also { logger.debug { "New event version $it" } }
    }

  override fun getLastVersion(aggregateId: AggregateId): Int =
    versionOfAggregate(aggregateId).toInt()

  private fun versionOfAggregate(aggregateId: AggregateId) =
    versions
      .computeIfAbsent(aggregateId) { AtomicInteger(0) }
}
