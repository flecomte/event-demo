package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
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
      try {
        controller.runOnlyOnce(command) {
          // Wrap action to add logs
          runAndLogStatus(command, action)
        }
      } catch (e: CommandRunnerController.Exception) {
        logger.atWarn {
          message = e.message
          payload = mapOf("command" to command)
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
      logger.atWarn {
        message = "Compute command FAILED: $command"
        payload = mapOf("command" to command)
        cause = actionResult.exceptionOrNull()
      }
    } else if (actionResult.isSuccess) {
      logger.atInfo {
        message = "Compute command SUCCESS: $command"
        payload = mapOf("command" to command)
      }
    }
  }
}

typealias CommandBlock<C> = suspend (C) -> Unit
