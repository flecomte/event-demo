package eventDemo

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

inline fun <T> withLogLevel(
  vararg logs: Pair<String, Level>,
  block: () -> T,
): T {
  val originalLevels =
    run {
      logs.toList().associate { (log, level) ->
        val logger = LoggerFactory.getLogger(log) as Logger
        log to logger.level
      }
    }

  logs.forEach { (log, level) ->
    val logger = LoggerFactory.getLogger(log) as Logger
    logger.level = level
  }

  val output = block()

  logs.forEach { (log, level) ->
    val logger = LoggerFactory.getLogger(log) as Logger
    logger.level = originalLevels[log]
  }

  return output
}
