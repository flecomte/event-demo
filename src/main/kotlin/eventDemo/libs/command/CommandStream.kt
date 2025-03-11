package eventDemo.libs.command

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Represent a Command stream.
 *
 * The stream contains a list of all actions yet to be executed.
 */
interface CommandStream<C : Command> {
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

typealias CommandBlock<C> = suspend CommandStream.ComputeStatus.(C) -> Unit
