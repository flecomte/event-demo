package eventDemo.business.command

import eventDemo.business.command.command.GameCommand
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventHandler
import eventDemo.business.event.event.GameEvent
import eventDemo.business.notification.CommandErrorNotification
import eventDemo.business.notification.CommandSuccessNotification
import eventDemo.business.notification.Notification
import eventDemo.libs.command.CommandId
import eventDemo.libs.command.CommandStreamChannel
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Listen [GameCommand] on [CommandStreamChannel], check the validity and execute an action.
 *
 * This action can be executing an action and produce a new [GameEvent] after verification.
 */
class GameCommandHandler(
  private val commandStreamChannel: CommandStreamChannel<GameCommand>,
  private val eventHandler: GameEventHandler,
  private val runner: GameCommandActionRunner,
) {
  private val logger = KotlinLogging.logger { }
  private val eventCommandMap = EventCommandMap()

  // subscribe to the event bus to send success notification after save the event.
  fun subscribeToBus(eventBus: GameEventBus) {
    eventBus.subscribe { event: GameEvent ->
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
    gameId: GameId,
    incomingCommandChannel: ReceiveChannel<GameCommand>,
    channelNotification: SendChannel<Notification>,
  ) {
    commandStreamChannel.process(incomingCommandChannel) { command ->
      withLoggingContext("command" to command.toString()) {
        if (command.payload.aggregateId.id != gameId.id) {
          logger.warn { "Handle command Refuse, the gameId of the command is not the same" }
          channelNotification.sendError(command)("The gameId in the command does not match with your game")
          return@process
        }

        if (command.payload.player.id != player.id) {
          logger.warn { "Handle command Refuse, the player of the command is not the same" }
          channelNotification.sendError(command)("You are not the author of this command")
          return@process
        }

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

private fun SendChannel<Notification>.sendError(command: GameCommand): suspend (message: String) -> Unit =
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
private class EventCommandMap(
  val retention: Duration = 10.minutes,
) {
  val map = ConcurrentHashMap<UUID, Output>()

  fun set(
    eventId: UUID,
    channel: SendChannel<Notification>,
    commandId: CommandId,
  ) {
    map[eventId] = Output(channel, commandId, Clock.System.now())

    map
      .filterValues { it.date < (Clock.System.now() - retention) }
      .keys
      .forEach(map::remove)
  }

  operator fun get(eventId: UUID): Output? =
    map[eventId]

  data class Output(
    val channel: SendChannel<Notification>,
    val commandId: CommandId,
    val date: Instant,
  )
}
