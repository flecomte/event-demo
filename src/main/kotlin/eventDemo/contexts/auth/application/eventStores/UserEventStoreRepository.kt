package eventDemo.contexts.auth.application.eventStores

import eventDemo.contexts.auth.application.ports.UserEventStore
import eventDemo.contexts.auth.domain.User
import eventDemo.sharedKernel.UserId

class UserEventStoreRepository(
  val eventStore: UserEventStore,
) : UserRepository {
  override fun get(id: UserId): User? {
    val events =
      eventStore
        .getStream(id)
        .readAll()
    if (events.isEmpty()) {
      return null
    }
    return events.let { User.loadFromHistory(it) }
  }

  override fun save(user: User) {
    eventStore.append(user.recordedEvents)
  }
}
