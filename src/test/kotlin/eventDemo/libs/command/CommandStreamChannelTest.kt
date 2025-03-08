package eventDemo.libs.command

import io.kotest.core.spec.style.FunSpec
import io.ktor.websocket.Frame
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class CommandTest(
    override val id: CommandId,
) : Command

class CommandStreamChannelTest :
    FunSpec({

        test("send and receive") {
            val command = CommandTest(CommandId())

            val channel = Channel<Frame>()
            val stream =
                CommandStreamChannel<CommandTest>(
                    incoming = channel,
                    deserializer = { Json.decodeFromString(it) },
                )

            val spyCall: () -> Unit = mockk(relaxed = true)

            stream.blockAndProcess {
                println("In action ${it.id}")
                spyCall()
            }
            channel.send(Frame.Text(Json.encodeToString(command)))
            verify(exactly = 1) { spyCall() }
        }
    })
