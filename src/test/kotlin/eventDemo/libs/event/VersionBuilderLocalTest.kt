package eventDemo.libs.event

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class VersionBuilderLocalTest :
    FunSpec({

        test("buildNextVersion") {
            VersionBuilderLocal().run {
                buildNextVersion() shouldBeEqual 1
                buildNextVersion() shouldBeEqual 2
                buildNextVersion() shouldBeEqual 3
            }
        }

        test("buildNextVersion concurrently") {
            val versionBuilder = VersionBuilderLocal()
            (1..20)
                .map {
                    GlobalScope.launch {
                        (1..1000).map {
                            versionBuilder.buildNextVersion()
                        }
                    }
                }.joinAll()
            versionBuilder.getLastVersion() shouldBeEqual 20 * 1000
        }

        test("getLastVersion") {
            VersionBuilderLocal().run {
                getLastVersion() shouldBeEqual 0
                getLastVersion() shouldBeEqual 0
                getLastVersion() shouldBeEqual 0
            }
        }
    })
