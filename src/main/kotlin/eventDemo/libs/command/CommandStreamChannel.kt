package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.trySendBlocking
import kotlin.reflect.KClass

/**
 * Manage [Command]'s with kotlin Channel
 */
class CommandStreamChannel<C : Command>(
    private val incoming: ReceiveChannel<Frame>,
    private val outgoing: SendChannel<Frame>,
    private val serializer: (C) -> String,
    private val deserializer: (String) -> C,
) : CommandStream<C> {
    private val logger = KotlinLogging.logger {}

    /**
     * Send a new [Command] to the queue.
     */
    override fun send(
        type: KClass<C>,
        command: C,
    ) {
        outgoing
            .trySendBlocking(Frame.Text(serializer(command)))
            .onSuccess {
                logger.atInfo {
                    message = "Command published: $command"
                    payload = mapOf("command" to command)
                }
            }.onFailure {
                logger.atError {
                    message = "Command FAILED: $command"
                    payload = mapOf("command" to command)
                }
            }
    }

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
                message = "Error on compute the Command"
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
            message = "Compute command SUCCESS and it removed of the stack"
            payload = mapOf("command" to command)
        }
//        outgoing.trySendBlocking(Frame.Text("Command executed successfully"))
    }

    private suspend fun markAsFailed(command: C) {
        logger.atWarn {
            message = "Compute command FAILED"
            payload = mapOf("command" to command)
        }
//        outgoing.trySendBlocking(Frame.Text("Command execution failed"))
    }
}
