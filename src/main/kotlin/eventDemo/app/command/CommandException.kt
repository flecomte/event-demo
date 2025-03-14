package eventDemo.app.command

class CommandException(
  override val message: String,
) : Exception(message)
