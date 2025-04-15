package eventDemo.business.command

import eventDemo.business.command.command.GameCommand
import eventDemo.business.entity.GameId
import eventDemo.business.entity.Player
import eventDemo.business.event.GameEventBus
import eventDemo.business.event.GameEventStore
import eventDemo.business.event.event.GameEvent
import eventDemo.business.notification.CommandErrorNotification
import eventDemo.business.notification.CommandSuccessNotification
import eventDemo.business.notification.Notification
import eventDemo.libs.command.CommandHandler
import eventDemo.libs.command.CommandRunnerController
import eventDemo.libs.event.EventHandlerImpl
import eventDemo.libs.event.VersionBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

/**
 * Listen [GameCommand] on [GameEventBus], check the validity and execute an action.
 *
 * This action can be executing an action and produce a new [GameEvent] after verification.
 */
class GameCommandHandler(
  eventBus: GameEventBus,
  eventStore: GameEventStore,
  versionBuilder: VersionBuilder,
  runner: GameCommandActionRunner,
) {
  private val logger = KotlinLogging.logger { }

  private val eventHandler =
    EventHandlerImpl(
      eventBus,
      eventStore,
      versionBuilder,
    )
  private val commandHandler =
    CommandHandler(
      CommandRunnerController<GameCommand>(),
      eventHandler,
    ) {
      runner.run(it)
    }

  /**
   * Subscribe to the [event bus][GameEventBus]
   * to send success [notification][Notification] after save the [event][GameEvent].
   */
  fun subscribeToBus(eventBus: GameEventBus) =
    commandHandler.subscribeToBus(eventBus)

  /**
   * Lisent incoming [command][GameCommand] from the [channel][ReceiveChannel],
   * run the command and publish the generated [event][GameEvent] to the bus.
   *
   * It restricts to run only once a command.
   *
   * If the command fail, send an [error notification][CommandErrorNotification],
   * if success, send a [success notification][CommandSuccessNotification]
   */
  suspend fun handleIncomingPlayerCommands(
    player: Player,
    gameId: GameId,
    incomingCommandChannel: ReceiveChannel<GameCommand>,
    channelNotification: SendChannel<Notification>,
  ) {
    for (command in incomingCommandChannel) {
      handle(
        player,
        gameId,
        command,
        channelNotification.sendSuccess(command),
        channelNotification.sendError(command),
      )
    }
  }

  /**
   * Run the [command] and publish the generated [event][GameEvent] to the bus.
   *
   * It restricts to run only once a command.
   *
   * If the command fail, send an [error notification][CommandErrorNotification],
   * if success, send a [success notification][CommandSuccessNotification]
   */
  suspend fun handle(
    player: Player,
    gameId: GameId,
    command: GameCommand,
    sendSuccess: suspend () -> Unit,
    sendError: suspend (message: String) -> Unit,
  ) {
    if (command.payload.aggregateId.id != gameId.id) {
      logger.warn { "Handle command Refuse, the gameId of the command is not the same" }
      sendError("The gameId in the command does not match with your game")
      return
    }
    if (command.payload.player.id != player.id) {
      logger.warn { "Handle command Refuse, the player of the command is not the same" }
      sendError("You are not the author of this command")
      return
    }

    commandHandler.handle(gameId, command) { _, error ->
      if (error != null) {
        sendError(error.message) // Business
      } else {
        sendSuccess()
      }
    }
  }
}

private fun SendChannel<Notification>.sendSuccess(command: GameCommand): suspend () -> Unit =
  {
    val logger = KotlinLogging.logger { }
    CommandSuccessNotification(commandId = command.id)
      .also { notification ->
        withLoggingContext("notification" to notification.toString(), "commandId" to command.id.toString()) {
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
