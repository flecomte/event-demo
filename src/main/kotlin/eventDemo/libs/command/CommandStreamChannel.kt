package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Manage [Command]'s with kotlin Channel
 */
class CommandStreamChannel<C : Command>(
    private val incoming: ReceiveChannel<Frame>,
    private val deserializer: (String) -> C,
) : CommandStream<C> {
    private val logger = KotlinLogging.logger {}

    override suspend fun process(action: CommandBlock<C>) {
//        incoming.consumeEach { commandAsFrame ->
//            if (commandAsFrame is Frame.Text) {
//                compute(deserializer(commandAsFrame.readText()), action)
//            }
//        }
        for (command in incoming) {
            if (command is Frame.Text) {
                compute(deserializer(command.readText()), action)
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
