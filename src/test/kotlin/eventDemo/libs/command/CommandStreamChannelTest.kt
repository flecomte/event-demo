package eventDemo.libs.command

import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Serializable
class CommandTest(
  override val id: CommandId,
) : Command

@OptIn(DelicateCoroutinesApi::class)
class CommandStreamChannelTest :
  FunSpec({

    test("send and receive") {
      val command = CommandTest(CommandId())

      val channel = Channel<CommandTest>()
      val stream = CommandStreamChannel(CommandRunnerController())

      val spyCall: () -> Unit = mockk(relaxed = true)

      GlobalScope.launch {
        stream.process(channel) {
          println("In action ${it.id}")
          spyCall()
        }
      }

      channel.send(command)

      eventually(3.seconds) {
        verify(exactly = 1) { spyCall() }
      }
    }
  })
