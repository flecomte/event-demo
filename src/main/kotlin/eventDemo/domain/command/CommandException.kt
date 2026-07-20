package eventDemo.domain.command

class CommandException(
  override val message: String,
) : Exception(message)
