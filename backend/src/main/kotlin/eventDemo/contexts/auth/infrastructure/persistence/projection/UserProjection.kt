package eventDemo.contexts.auth.infrastructure.persistence.projection

import eventDemo.shared.ids.UserId

data class UserProjection(
  val id: UserId,
  val username: String,
  val password: String,
)
