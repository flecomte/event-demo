package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

typealias CommandBlock<C> = suspend CommandStream.ComputeStatus.(C) -> Unit

/**
 * Manage [Command]'s
 *
 * It stores the new [Command] in memory.
 */
abstract class CommandStreamInMemory<C : Command> : CommandStream<C> {
    private val logger = KotlinLogging.logger {}
    private val queue: Channel<C> =
        Channel(onUndeliveredElement = {
            logger.atWarn { "${it::class.simpleName} command not send" }
        })

    override suspend fun process(action: CommandBlock<C>) {
        queue.consumeEach { command ->
            compute(command, action)
        }
        for (command in queue) {
            compute(command, action)
        }
    }

    private suspend fun compute(
        command: C,
        action: CommandBlock<C>,
    ) {
        val status =
            object : CommandStream.ComputeStatus {
                var isSet: Boolean = false

                override suspend fun ack() {
                    if (!isSet) markAsSuccess(command) else error("Already NACK")
                    isSet = true
                }

                override suspend fun nack() {
                    if (!isSet) markAsFailed(command) else error("Already ACK")
                    isSet = true
                }
            }

        if (runCatching { status.action(command) }.isFailure) {
            markAsFailed(command)
        } else if (!status.isSet) {
            status.ack()
        }
    }

    private fun <C : Command> markAsSuccess(command: C) {
        logger.atInfo {
            message = "Compute command SUCCESS : $command"
            payload = mapOf("command" to command)
        }
    }

    private fun <C : Command> markAsFailed(command: C) {
        logger.atWarn {
            message = "Compute command FAILED : $command"
            payload = mapOf("command" to command)
        }
    }
}
