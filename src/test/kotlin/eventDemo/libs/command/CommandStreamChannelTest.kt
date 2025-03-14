package eventDemo.libs.command

import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable

@Serializable
class CommandTest(
  override val id: CommandId,
) : Command

class CommandStreamChannelTest :
  FunSpec({

    test("send and receive") {
      val command = CommandTest(CommandId())

      val channel = Channel<CommandTest>()
      val stream =
        CommandStreamChannel(channel)

      val spyCall: () -> Unit = mockk(relaxed = true)

      stream.blockAndProcess {
        println("In action ${it.id}")
        spyCall()
      }
      channel.send(command)
      verify(exactly = 1) { spyCall() }
    }
  })
