package eventDemo.libs

import eventDemo.libs.command.Command
import eventDemo.libs.command.CommandId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.test.assertIs

@Serializable
data class CommandTest(
  override val id: CommandId,
) : Command

class FrameChannelConverterTest :
  FunSpec({

    test("toObjectChannel") {
      val uuid = "d737c631-76af-406e-bc29-f3e5b97226a5"
      val id = CommandId(UUID.fromString(uuid))
      val jsonCommand = """{"id":"$uuid"}"""

      val channel = Channel<Frame>()

      launch {
        val commandChannel = toObjectChannel<CommandTest>(channel)
        commandChannel.receive().id shouldBeEqual id
        channel.close()
      }

      channel.send(Frame.Text(jsonCommand))
    }

    test("fromFrameChannel") {
      val uuid = "d737c631-76af-406e-bc29-f3e5b97226a5"
      val id = CommandId(UUID.fromString(uuid))
      val command = CommandTest(id)
      val jsonCommand = """{"id":"$uuid"}"""

      val channel = Channel<Frame>()

      launch {
        val commandChannel = fromFrameChannel<CommandTest>(channel)
        commandChannel.send(command)
        commandChannel.close()
      }

      assertIs<Frame.Text>(channel.receive()).readText() shouldBeEqual jsonCommand
    }
  })
