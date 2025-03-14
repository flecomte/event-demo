package eventDemo.libs.event

import io.kotest.core.spec.style.FunSpec

class EventStreamInMemoryTest :
  FunSpec({

    xtest("publish should be concurrently secure") { }

    xtest("readLast should only return the event of aggregate") { }
    xtest("readLast should return the last event of the aggregate") { }

    xtest("readLastOf should return the last event of the aggregate of the type") { }

    xtest("readAll should only return the event of aggregate") { }
  })
