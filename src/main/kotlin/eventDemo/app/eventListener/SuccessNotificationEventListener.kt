package eventDemo.app.eventListener

import eventDemo.app.notification.CommandSuccessNotification
import eventDemo.app.notification.Notification
import eventDemo.libs.command.CommandId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.SendChannel

private fun SendChannel<Notification>.successNotifier(commandId: CommandId): suspend () -> Unit =
  {
    val logger = KotlinLogging.logger { }
    CommandSuccessNotification(commandId = commandId)
      .let { notification ->
        logger.atDebug {
          message = "Notification SUCCESS sent"
          payload =
            mapOf(
              "notification" to notification,
              "commandId" to commandId,
            )
        }
        send(notification)
      }
  }
