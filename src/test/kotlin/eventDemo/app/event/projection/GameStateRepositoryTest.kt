package eventDemo.app.event.projection

import io.kotest.core.spec.style.FunSpec

class GameStateRepositoryTest :
    FunSpec({
        xtest("GameStateRepository should build the projection when a new event occurs") { }

        xtest("get should build the last version of the state") { }
        xtest("get should be concurrently secure") { }
        xtest("get should be concurrently secure") { }

        xtest("getUntil should build the state until the event") { }
        xtest("call getUntil twice should get the state from the cache") { }
        xtest("getUntil should be concurrently secure") { }
    })
