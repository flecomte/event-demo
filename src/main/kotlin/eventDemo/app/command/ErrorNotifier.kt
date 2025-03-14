package eventDemo.app.command

import eventDemo.app.command.command.GameCommand
import eventDemo.app.notification.ErrorNotification
import eventDemo.app.notification.Notification
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.SendChannel

typealias ErrorNotifier = suspend (String) -> Unit

fun errorNotifier(
  command: GameCommand,
  channel: SendChannel<Notification>,
): ErrorNotifier =
  {
    val logger = KotlinLogging.logger { }
    ErrorNotification(message = it)
      .let { notification ->
        logger.atWarn {
          message = "Notification ERROR sent: ${notification.message}"
          payload =
            mapOf(
              "notification" to notification,
              "command" to command,
            )
        }
        channel.send(notification)
      }
  }
