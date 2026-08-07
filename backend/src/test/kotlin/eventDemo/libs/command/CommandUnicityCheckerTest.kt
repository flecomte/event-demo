package eventDemo.libs.command

import eventDemo.shared.ids.CommandId
import eventDemo.testHelpers.spyPing
import io.kotest.core.spec.style.FunSpec
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.seconds

class CommandUnicityCheckerTest :
  FunSpec({
    test("runOnlyOnce must run all commands") {
      spyPing(exactly = 2, duration = 3.seconds) { ping ->
        val com1 = CommandForTest(CommandId())
        val com2 = CommandForTest(CommandId())
        CommandUnicityChecker<CommandForTest>().run {
          runOnlyOnce(com1) { ping() }
          runOnlyOnce(com2) { ping() }
        }
      }
    }

    test("runOnlyOnce") {
      spyPing(exactly = 2, duration = 3.seconds) { ping ->
        val com1 = CommandForTest(CommandId())
        val com2 = CommandForTest(CommandId())
        CommandUnicityChecker<CommandForTest>().run {
          runOnlyOnce(com1) { ping() }
          runOnlyOnce(com2) { ping() }
          assertThrows<CommandUnicityChecker.UnicityException> {
            runOnlyOnce(com2) { ping() }
          }
        }
      }
    }
  })
