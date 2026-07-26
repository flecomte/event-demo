package eventDemo.libs.helpers

import eventDemo.libs.command.CommandForTest
import eventDemo.libs.command.CommandId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.test.assertIs

class FrameChannelConverterTest :
  FunSpec({

    test("toObjectChannel") {
      val uuid = "d737c631-76af-406e-bc29-f3e5b97226a5"
      val id = CommandId(UUID.fromString(uuid))
      val jsonCommand = """{"id":"$uuid"}"""

      val channel = Channel<Frame>()

      launch {
        val commandChannel = toObjectChannel<CommandForTest>(channel)
        commandChannel.receive().id shouldBeEqual id
        channel.close()
      }

      channel.send(Frame.Text(jsonCommand))
    }

    test("fromFrameChannel") {
      val uuid = "d737c631-76af-406e-bc29-f3e5b97226a5"
      val id = CommandId(UUID.fromString(uuid))
      val command = CommandForTest(id)
      val jsonCommand = """{"id":"$uuid"}"""

      val channel = Channel<Frame>()

      launch {
        val commandChannel = fromFrameChannel<CommandForTest>(channel)
        commandChannel.send(command)
        commandChannel.close()
      }

      assertIs<Frame.Text>(channel.receive()).readText() shouldBeEqual jsonCommand
    }
  })
