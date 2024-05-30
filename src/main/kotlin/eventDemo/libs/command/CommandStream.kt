package eventDemo.libs.command

import kotlin.reflect.KClass

interface CommandStream<C : Command> {
    /**
     * Send a new [Command] to the queue.
     */
    suspend fun send(
        type: KClass<C>,
        command: C,
    )

    /**
     * Send multiple [Command] to the queue.
     */
    suspend fun send(
        type: KClass<C>,
        vararg commands: C,
    ) {
        commands.forEach { send(type, it) }
    }

    /**
     * A class to implement succes/faild action.
     */
    interface ComputeStatus {
        fun ack()

        fun nack()
    }

    suspend fun process(block: CommandBlock<C>)
}

suspend inline fun <reified C : Command> CommandStream<C>.send(vararg command: C) = send(C::class, *command)
