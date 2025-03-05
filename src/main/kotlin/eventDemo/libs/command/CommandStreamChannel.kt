package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
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
    private val failedCommand = mutableListOf<C>()

    /**
     * Send a new [Command] to the queue.
     */
    override suspend fun send(
        type: KClass<C>,
        command: C,
    ) {
        outgoing.send(Frame.Text(serializer(command)))
        logger.atInfo {
            message = "Command published: $command"
            payload = mapOf("command" to command)
        }
    }

    override suspend fun process(action: CommandStream.ComputeStatus.(C) -> Unit) {
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
        action: CommandStream.ComputeStatus.(C) -> Unit,
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

        val action = runCatching { status.action(command) }
        if (action.isFailure) {
            logger.atInfo {
                message = "Error"
                payload = mapOf("command" to command)
                cause = action.exceptionOrNull()
            }
            markAsFailed(command)
        } else if (!status.isSet) {
            status.ack()
        }
    }

    private suspend fun markAsSuccess(command: C) {
        logger.atInfo {
            message = "Compute command SUCCESS and it removed of the stack : $command"
            payload = mapOf("command" to command)
        }
        GlobalScope.launch {
//            outgoing.send(Frame.Text("Command executed successfully"))
        }
    }

    private suspend fun markAsFailed(command: C) {
        failedCommand.add(command)
        logger.atWarn {
            message = "Compute command FAILED and it put it ot the top of the stack : $command"
            payload = mapOf("command" to command)
        }
        outgoing.send(Frame.Text("Command execution failed"))
    }
}
