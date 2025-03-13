package eventDemo.libs.event

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.UUID

@JvmInline
private value class IdTest(
    override val id: UUID = UUID.randomUUID(),
) : AggregateId

@OptIn(DelicateCoroutinesApi::class)
class VersionBuilderLocalTest :
    FunSpec({

        test("buildNextVersion") {
            VersionBuilderLocal().run {
                val id = IdTest()
                buildNextVersion(id) shouldBeEqual 1
                buildNextVersion(id) shouldBeEqual 2
                buildNextVersion(IdTest()) shouldBeEqual 1
                buildNextVersion(id) shouldBeEqual 3
            }
        }

        test("buildNextVersion concurrently") {
            val versionBuilder = VersionBuilderLocal()
            val id = IdTest()
            (1..20)
                .map {
                    GlobalScope.launch {
                        (1..1000).map {
                            versionBuilder.buildNextVersion(id)
                        }
                    }
                }.joinAll()
            versionBuilder.getLastVersion(id) shouldBeEqual 20 * 1000
        }

        test("getLastVersion") {
            VersionBuilderLocal().run {
                val id = IdTest()
                getLastVersion(id) shouldBeEqual 0
                getLastVersion(id) shouldBeEqual 0
                getLastVersion(id) shouldBeEqual 0
            }
        }
    })
