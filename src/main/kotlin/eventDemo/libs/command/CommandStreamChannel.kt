package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class CommandStreamChannelBuilder<C : Command>(
    private val maxCacheTime: Duration = 10.minutes,
) {
    operator fun invoke(incoming: ReceiveChannel<C>): CommandStreamChannel<C> = CommandStreamChannel(incoming, maxCacheTime)
}

/**
 * Manage [Command]'s with kotlin Channel
 */
class CommandStreamChannel<C : Command>(
    private val incoming: ReceiveChannel<C>,
    private val maxCacheTime: Duration = 10.minutes,
) : CommandStream<C> {
    private val logger = KotlinLogging.logger {}
    private val executedCommand: ConcurrentHashMap<CommandId, Pair<Boolean, Instant>> = ConcurrentHashMap()

    override suspend fun process(action: CommandBlock<C>) {
        for (command in incoming) {
            val now = Clock.System.now()
            val (status, _) = executedCommand.computeIfAbsent(command.id) { Pair(false, now) }

            if (status) {
                logger.atWarn {
                    message = "Command already executed: $command"
                    payload = mapOf("command" to command)
                }
            } else {
                compute(command, action)
            }
            executedCommand
                .filterValues { (_, date) ->
                    (date + maxCacheTime) > now
                }.keys
                .forEach {
                    executedCommand.remove(it)
                }
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

        val actionResult = runCatching { status.action(command) }
        if (actionResult.isFailure) {
            logger.atInfo {
                message = "Error on compute the Command: $command"
                payload = mapOf("command" to command)
                cause = actionResult.exceptionOrNull()
            }
            markAsFailed(command)
        } else if (!status.isSet) {
            status.ack()
        }
    }

    private suspend fun markAsSuccess(command: C) {
        logger.atInfo {
            message = "Compute command SUCCESS: $command"
            payload = mapOf("command" to command)
        }
    }

    private suspend fun markAsFailed(command: C) {
        logger.atWarn {
            message = "Compute command FAILED: $command"
            payload = mapOf("command" to command)
        }
    }
}
