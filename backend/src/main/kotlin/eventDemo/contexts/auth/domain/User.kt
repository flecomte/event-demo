package eventDemo.contexts.auth.domain

import eventDemo.contexts.auth.domain.events.NewUserCreatedEvent
import eventDemo.contexts.auth.domain.events.UserEvent
import eventDemo.shared.ids.UserId
import kotlinx.serialization.Serializable

@Serializable
data class User(
  val id: UserId,
  val username: String,
  val password: String,
  val version: Int,
  val recordedEvents: Set<UserEvent>,
) {
  companion object {
    fun createNewUser(
      username: String,
      password: String,
    ): User =
      apply(NewUserCreatedEvent(username, password, version = 1))

    fun apply(event: NewUserCreatedEvent): User =
      User(
        id = event.aggregateId,
        username = event.username,
        password = event.password,
        version = event.version,
        recordedEvents = setOf(event),
      )

    fun loadFromHistory(events: Set<UserEvent>): User? =
      events.fold(null as User?) { acc, event ->
        when (event) {
          is NewUserCreatedEvent -> apply(event)
        }
      }
  }
}
