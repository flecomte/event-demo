package eventDemo.contexts.auth.application.ports

import eventDemo.contexts.auth.infrastructure.persistence.projection.UserProjection

interface UserProjectionRepository {
  fun getByUsername(username: String): UserProjection?

  fun save(user: UserProjection)

  fun getUserIfPasswordIsValid(
    username: String,
    rawPassword: String,
  ): UserProjection?
}
