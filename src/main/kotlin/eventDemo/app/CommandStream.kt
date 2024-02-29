package eventDemo.app

import io.github.oshai.kotlinlogging.KotlinLogging

class CommandStream {
    private val logger = KotlinLogging.logger {}
    private val commandBus: MutableList<Command> = mutableListOf()

    fun sendRequest(command: Command) {
        commandBus.add(command)
        logger.atInfo {
            message = "Command published: $command"
            payload = mapOf("command" to command)
        }
    }

    fun sendRequest(vararg commands: Command) {
        commands.forEach { sendRequest(it) }
    }

    fun readNext(): Command? = commandBus.firstOrNull()

    fun <U : Command> readNext(commandClass: Class<U>): U? = commandBus.filterIsInstance(commandClass).firstOrNull()
}
