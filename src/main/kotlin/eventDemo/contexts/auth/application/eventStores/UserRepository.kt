package eventDemo.contexts.auth.application.eventStores

import eventDemo.contexts.auth.domain.User
import eventDemo.sharedKernel.UserId

interface UserRepository {
  fun get(id: UserId): User?

  fun save(user: User)
}
