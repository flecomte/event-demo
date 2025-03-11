package eventDemo.app.event

import io.kotest.core.spec.style.FunSpec

class GameEventHandlerTest :
    FunSpec({
        xtest("handle event should publish the event to the stream") { }
        xtest("handle event should build the registered projection") { }
        xtest("handle event should publish the event to the bus") { }
    })
