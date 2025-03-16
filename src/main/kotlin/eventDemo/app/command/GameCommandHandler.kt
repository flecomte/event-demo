package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventBus
import eventDemo.app.event.GameEventHandler
import eventDemo.app.event.event.GameEvent
import eventDemo.app.notification.CommandErrorNotification
import eventDemo.app.notification.CommandSuccessNotification
import eventDemo.app.notification.Notification
import eventDemo.libs.command.CommandId
import eventDemo.libs.command.CommandStreamChannel
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Listen [GameCommand] on [CommandStreamChannel], check the validity and execute an action.
 *
 * This action can be executing an action and produce a new [GameEvent] after verification.
 */
class GameCommandHandler(
  private val commandStreamChannel: CommandStreamChannel<GameCommand>,
  private val eventHandler: GameEventHandler,
  private val runner: GameCommandActionRunner,
  eventBus: GameEventBus,
  listenerPriority: Int = DEFAULT_PRIORITY,
) {
  private val logger = KotlinLogging.logger { }
  private val eventCommandMap = EventCommandMap()

  companion object Config {
    const val DEFAULT_PRIORITY = 1000
  }

  // subscribe to the event bus to send success notification after save the event.
  init {
    eventBus.subscribe(listenerPriority) { event: GameEvent ->
      eventCommandMap[event.eventId]?.apply {
        channel.sendSuccess(commandId)()
      } ?: logger.warn { "No Notification for event: $event" }
    }
  }

  /**
   * Run a command and publish the event.
   *
   * It restricts to run only once a command.
   *
   * If the command fail, send an [error notification][CommandErrorNotification],
   * if success, send a [success notification][CommandSuccessNotification]
   */
  suspend fun handle(
    player: Player,
    incomingCommandChannel: ReceiveChannel<GameCommand>,
    channelNotification: SendChannel<Notification>,
  ) {
    commandStreamChannel.process(incomingCommandChannel) { command ->
      withLoggingContext("command" to command.toString()) {
        if (command.payload.player.id != player.id) {
          logger.warn { "Handle command Refuse, the player of the command is not the same" }
          channelNotification.sendError(command)("You are not the author of this command\n")
        } else {
          logger.info { "Handle command" }
          try {
            val eventBuilder = runner.run(command)

            eventHandler.handle(command.payload.aggregateId) { version ->
              eventBuilder(version)
                .also { eventCommandMap.set(it.eventId, channelNotification, command.id) }
            }
          } catch (e: CommandException) {
            logger.warn(e) { e.message }
            channelNotification.sendError(command)(e.message)
          }
        }
      }
    }
  }
}

private fun SendChannel<Notification>.sendSuccess(commandId: CommandId): suspend () -> Unit =
  {
    val logger = KotlinLogging.logger { }
    CommandSuccessNotification(commandId = commandId)
      .also { notification ->
        withLoggingContext("notification" to notification.toString(), "commandId" to commandId.toString()) {
          logger.debug { "Notification SUCCESS sent" }
          send(notification)
        }
      }
  }

private fun SendChannel<Notification>.sendError(command: GameCommand): suspend (String) -> Unit =
  {
    val logger = KotlinLogging.logger { }
    CommandErrorNotification(message = it, command = command)
      .also { notification ->
        withLoggingContext("notification" to notification.toString(), "command" to command.toString()) {
          logger.warn { "Notification ERROR sent: ${notification.message}" }
          send(notification)
        }
      }
  }

/**
 * Map to record the command that triggered the event.
 */
private class EventCommandMap {
  val map = ConcurrentHashMap<UUID, Output>()

  fun set(
    eventId: UUID,
    channel: SendChannel<Notification>,
    commandId: CommandId,
  ) {
    map[eventId] = Output(channel, commandId)
  }

  operator fun get(eventId: UUID): Output? =
    map[eventId]

  data class Output(
    val channel: SendChannel<Notification>,
    val commandId: CommandId,
  )
}
