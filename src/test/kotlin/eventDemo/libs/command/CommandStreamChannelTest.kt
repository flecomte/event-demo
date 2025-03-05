package eventDemo.libs.command

import io.kotest.core.spec.style.FunSpec
import io.ktor.websocket.Frame
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CommandTest(
    override val id: CommandId,
) : Command

class CommandStreamChannelTest :
    FunSpec({

        test("send and receive") {
            val command = CommandTest(CommandId())
            val command2 = CommandTest(CommandId())
            val command3 = CommandTest(CommandId())

            val channel = Channel<Frame>()
            val stream =
                CommandStreamChannel<CommandTest>(
                    incoming = channel,
                    outgoing = channel,
                    serializer = { it.id.toString() },
                    deserializer = { CommandTest(CommandId(it)) },
                )

            val spyCall: () -> Unit = mockk(relaxed = true)
            runBlocking {
                launch {
                    stream.process {
                        println("In action ${it.id}")
                        spyCall()
                    }
                }
                launch {
                    stream.send(command, command2)
                    stream.send(command3)
                    channel.close()
                }.join()
                verify(exactly = 3) { spyCall() }
            }
        }
    })
