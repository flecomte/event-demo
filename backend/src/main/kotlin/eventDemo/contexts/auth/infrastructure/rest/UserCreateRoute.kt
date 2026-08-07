package eventDemo.contexts.auth.infrastructure.rest

import eventDemo.contexts.auth.application.eventStores.UserRepository
import eventDemo.contexts.auth.domain.User
import eventDemo.contexts.auth.infrastructure.hashPassword
import io.ktor.resources.Resource
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class Users {
  @Serializable
  @Resource("/create")
  class Create(
    val username: String,
    val password: String,
  )
}

/**
 * API routes to show all games.
 */
fun Route.createUserRoute(userRepository: UserRepository) {
  authenticate {
    // Create a new User, and return there ID
    post<Users.Create> {
      val passwordHash = hashPassword(it.password)
      val user = User.createNewUser(it.username, passwordHash.result)
      userRepository.save(user)
      call.respond(
        object {
          val id = user.id.toString()
        },
      )
    }
  }
}
