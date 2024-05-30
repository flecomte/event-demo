package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlin.reflect.KClass

typealias CommandBlock<C> = CommandStream.ComputeStatus.(C) -> Unit

/**
 * Manage [Command]'s
 *
 * It stores the new [Command] in memory.
 */
abstract class CommandStreamInMemory<C : Command> : CommandStream<C> {
    private val logger = KotlinLogging.logger {}
    private val failedCommand = mutableListOf<Command>()
    private val queue: Channel<C> = Channel(onUndeliveredElement = { logger.atWarn { "${it.name} elem not send" } })

    /**
     * Send a new [Command] to the queue.
     */
    override suspend fun send(
        type: KClass<C>,
        command: C,
    ) {
        logger.atInfo {
            message = "Command published: $command"
            payload = mapOf("command" to command)
        }
        queue.send(command)
    }

    override suspend fun process(block: CommandBlock<C>) {
        queue.consumeEach { command ->
            compute(command, block)
        }
        for (command in queue) {
            compute(command, block)
        }
    }

    private fun compute(
        command: C,
        block: CommandBlock<C>,
    ) {
        val status = object : CommandStream.ComputeStatus {
            var isSet: Boolean = false

            override fun ack() {
                if (!isSet) markAsSuccess(command) else error("Already NACK")
                isSet = true
            }

            override fun nack() {
                if (!isSet) markAsFailed(command) else error("Already ACK")
                isSet = true
            }
        }

        if (runCatching { status.block(command) }.isFailure) {
            markAsFailed(command)
        } else if (!status.isSet) {
            status.ack()
        }
    }

    private fun <C : Command> markAsSuccess(command: C) {
        logger.atInfo {
            message = "Compute command SUCCESS and it removed of the stack : $command"
            payload = mapOf("command" to command)
        }
    }

    private fun <C : Command> markAsFailed(command: C) {
        failedCommand.add(command)
        logger.atWarn {
            message = "Compute command FAILD and it put on the top of the stack : $command"
            payload = mapOf("command" to command)
        }
    }
}
