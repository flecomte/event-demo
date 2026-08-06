package eventDemo.contexts.auth.domain

import eventDemo.contexts.auth.domain.events.NewUserCreatedEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.assertNotNull

class UserTest :
  FunSpec({

    test("Create User") {
      User.createNewUser("Bob", "changeit").run {
        username shouldBe "Bob"
        password shouldBe "changeit"
      }
    }

    test("Create User With event") {
      User
        .loadFromHistory(
          setOf(
            NewUserCreatedEvent(
              "Bob",
              "changeit",
              version = 1,
            ),
          ),
        ).run {
          assertNotNull(this)
          username shouldBe "Bob"
          password shouldBe "changeit"
        }
    }
  })
