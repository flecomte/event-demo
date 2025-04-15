package eventDemo.libs.command

import eventDemo.business.command.CommandException
import eventDemo.business.command.command.GameCommand
import eventDemo.business.event.event.GameEvent
import eventDemo.libs.bus.Bus
import eventDemo.libs.event.AggregateId
import eventDemo.libs.event.Event
import eventDemo.libs.event.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
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
class CommandHandler<B : Bus<E>, E : Event<ID>, ID : AggregateId, C : Command>(
  private val controller: CommandRunnerController<C>,
  private val eventHandler: EventHandler<E, ID>,
  private val runner: (command: C) -> (version: Int) -> E,
) {
  private val logger = KotlinLogging.logger { }
  private val eventCommandMap = EventCommandMap<C, E>()

  /** subscribe to the event bus to run callback after event was saved. */
  fun subscribeToBus(eventBus: B) {
    eventBus.subscribe { event: E ->
      eventCommandMap[event.eventId]?.invoke()
        ?: logger.debug { "No Notification for event: $event" }
    }
  }

  /**
   * Run the [command] and publish generated [event][Event].
   *
   * The [callback] is call after execute the [command]
   *
   * It restricts to run only once the [command].
   */
  suspend fun handle(
    aggregateId: ID,
    command: C,
    callback: CommandCallback<C>,
  ) {
    controller.runOnlyOnce(command) {
      withLoggingContext("command" to command.toString()) {
        logger.info { "Handle command" }
        try {
          val eventBuilder = runner(command)

          eventHandler.handle(aggregateId) { version ->
            eventBuilder(version)
              .also { eventCommandMap.set(callback, it, command) }
          }
        } catch (e: CommandException) {
          logger.warn(e) { e.message }
          callback(command, e)
        }
      }
    }
  }
}

/**
 * Map to record the command that triggered the event.
 */
private class EventCommandMap<C : Command, E : Event<*>>(
  val retention: Duration = 10.minutes,
) {
  val map = ConcurrentHashMap<UUID, Callback<C, E>>()

  fun set(
    callback: CommandCallback<C>,
    event: E,
    command: C,
  ) {
    map[event.eventId] = Callback(callback, command, event, Clock.System.now())

    // Remove older
    map
      .filterValues { it.date < (Clock.System.now() - retention) }
      .keys
      .forEach(map::remove)
  }

  operator fun get(eventId: UUID): Callback<C, E>? =
    map[eventId]

  data class Callback<C : Command, E : Event<*>>(
    val callback: CommandCallback<C>,
    val command: C,
    val event: E,
    val date: Instant,
  ) {
    suspend operator fun invoke(error: CommandException? = null) {
      callback(command, error)
    }
  }
}

typealias CommandCallback<C> = suspend (command: C, error: CommandException?) -> Unit
