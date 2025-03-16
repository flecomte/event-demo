package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Manage [Command]'s with kotlin Channel.
 *
 * Use [CommandRunnerController] to prevent multiple executions.
 *
 * Add logs when command success or failed
 */
class CommandStreamChannel<C : Command>(
  private val controller: CommandRunnerController<C>,
) {
  private val logger = KotlinLogging.logger {}

  suspend fun process(
    incoming: ReceiveChannel<C>,
    action: CommandBlock<C>,
  ) {
    for (command in incoming) {
      withLoggingContext("command" to command.toString()) {
        try {
          controller.runOnlyOnce(command) {
            // Wrap action to add logs
            runAndLogStatus(command, action)
          }
        } catch (e: CommandRunnerController.Exception) {
          logger.warn { e.message }
        }
      }
    }
  }

  private suspend fun runAndLogStatus(
    command: C,
    action: CommandBlock<C>,
  ) {
    val actionResult = runCatching { action(command) }
    if (actionResult.isFailure) {
      logger.warn(actionResult.exceptionOrNull()) { "Compute command FAILED" }
    } else if (actionResult.isSuccess) {
      logger.info { "Compute command SUCCESS" }
    }
  }
}

typealias CommandBlock<C> = suspend (C) -> Unit
