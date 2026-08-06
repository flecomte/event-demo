package eventDemo.shared.command

import eventDemo.shared.ids.CommandId

/**
 * Interface to represent a Command.
 *
 * A command is a request for an action.
 *
 * Moved to `shared` (deviating slightly from the original plan of leaving it backend-only)
 * because [eventDemo.shared.game.command.GameCommand] - a wire type that must live in `shared`
 * for multiplatform client reuse - implements it. Since `shared` cannot depend on `backend`,
 * this minimal marker interface has to live wherever its implementers live.
 */
interface Command {
  val id: CommandId
}
