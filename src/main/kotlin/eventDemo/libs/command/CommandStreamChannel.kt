package eventDemo.libs.command

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
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
        logger.atInfo {
            message = "Command published: $command"
            payload = mapOf("command" to command)
        }

        outgoing.send(Frame.Text(serializer(command)))
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

    private fun compute(
        command: C,
        action: CommandStream.ComputeStatus.(C) -> Unit,
    ) {
        val status =
            object : CommandStream.ComputeStatus {
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

        if (runCatching { status.action(command) }.isFailure) {
            markAsFailed(command)
        } else if (!status.isSet) {
            status.ack()
        }
    }

    private fun markAsSuccess(command: C) {
        logger.atInfo {
            message = "Compute command SUCCESS and it removed of the stack : $command"
            payload = mapOf("command" to command)
        }
        runBlocking {
            outgoing.send(Frame.Text("Command executed successfully"))
        }
    }

    private fun markAsFailed(command: C) {
        failedCommand.add(command)
        logger.atWarn {
            message = "Compute command FAILED and it put it ot the top of the stack : $command"
            payload = mapOf("command" to command)
        }
        runBlocking {
            outgoing.send(Frame.Text("Command execution failed"))
        }
    }
}
