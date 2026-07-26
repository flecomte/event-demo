package eventDemo.contexts.game.application.command.handlers

class CommandException(
  override val message: String,
) : Exception(message)
