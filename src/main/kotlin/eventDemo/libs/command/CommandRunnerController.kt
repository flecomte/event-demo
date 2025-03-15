package eventDemo.libs.command

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Controls the execution of a command to prevent it from being executed more than once.
 */
class CommandRunnerController<C : Command>(
  private val maxCacheTime: Duration = 10.minutes,
) {
  private val executedCommand: ConcurrentHashMap<CommandId, Pair<Boolean, Instant>> = ConcurrentHashMap()

  suspend fun runOnlyOnce(
    command: C,
    action: CommandBlock<C>,
  ) {
    if (!isAlreadyExecuted(command)) {
      action(command)
      setAsExecuted(command)
      removeOldCache()
    } else {
      throw Exception("Command already executed", command)
    }
  }

  private fun setAsExecuted(command: C) {
    executedCommand.computeIfAbsent(command.id) { Pair(false, Clock.System.now()) }
  }

  private fun removeOldCache() {
    executedCommand
      .filterValues { (_, date) ->
        (date + maxCacheTime) > Clock.System.now()
      }.keys
      .forEach {
        executedCommand.remove(it)
      }
  }

  private fun isAlreadyExecuted(command: C): Boolean =
    executedCommand[command.id]?.first ?: false

  class Exception(
    override val message: String,
    val command: Command,
  ) : kotlin.Exception(message)
}
