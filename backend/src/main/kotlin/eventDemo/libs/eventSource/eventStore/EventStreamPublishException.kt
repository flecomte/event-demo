package eventDemo.libs.eventSource.eventStore

class EventStreamPublishException(
  override val message: String,
) : Exception(message)
