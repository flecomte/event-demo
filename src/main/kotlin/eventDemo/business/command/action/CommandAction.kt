package eventDemo.business.command.action

import eventDemo.libs.command.Command
import eventDemo.libs.event.Event

sealed interface CommandAction<C : Command, E : Event<*>> {
  fun run(command: C): (version: Int) -> E
}
