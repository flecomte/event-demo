package eventDemo.testHelpers

import eventDemo.contexts.auth.domain.User

fun createNewUser(name: String): User =
  User
    .createNewUser(name, "changeit")
