package eventDemo.libs.helpers

import eventDemo.libs.command.CommandForTest
import eventDemo.shared.ids.CommandId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.test.assertIs
import kotlin.uuid.Uuid

class FrameChannelConverterTest :
  FunSpec({

    test("toObjectChannel") {
      val uuid = "d737c631-76af-406e-bc29-f3e5b97226a5"
      val id = CommandId(Uuid.parse(uuid))
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
      val id = CommandId(Uuid.parse(uuid))
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
