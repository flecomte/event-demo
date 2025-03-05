package eventDemo.libs.command

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Represent a Command stream.
 *
 * The stream contains a list of all actions yet to be executed.
 */
interface CommandStream<C : Command> {
    /**
     * Send a new [Command] to the queue.
     */
    fun send(
        type: KClass<C>,
        command: C,
    )

    /**
     * Send multiple [Command] to the queue.
     */
    fun send(
        type: KClass<C>,
        vararg commands: C,
    ) {
        commands.forEach { send(type, it) }
    }

    /**
     * A class to implement success/failed action.
     */
    interface ComputeStatus {
        suspend fun ack()

        suspend fun nack()
    }

    /**
     * Apply an action to all command income in the stream.
     */
    suspend fun process(action: CommandBlock<C>)

    @OptIn(DelicateCoroutinesApi::class)
    fun blockAndProcess(action: CommandBlock<C>) {
        GlobalScope.launch {
            process(action)
        }
    }
}

suspend inline fun <reified C : Command> CommandStream<C>.send(vararg command: C) = send(C::class, *command)
