package eventDemo.libs.event.projection

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class SnapshotConfig(
  /**
   * Keep snapshot when is on the head of the queue cache
   */
  val maxSnapshotCacheSize: Int = 20,
  /**
   * Keep snapshot when is newer of
   *
   *     snapshot.date > now + maxSnapshotCacheTtl
   */
  val maxSnapshotCacheTtl: Duration = 10.minutes,
  /**
   * Keep snapshot when version is this modulo
   *
   *     snapshot.lastVersion % modulo == 1
   */
  val modulo: Int = 10,
  val enabled: Boolean = true,
)

val DISABLED_CONFIG = SnapshotConfig(Int.MAX_VALUE, Duration.INFINITE, Int.MAX_VALUE, enabled = false)
