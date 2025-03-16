package eventDemo.business.command

class CommandException(
  override val message: String,
) : Exception(message)
