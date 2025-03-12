package eventDemo.libs.event

import java.util.concurrent.atomic.AtomicInteger

class VersionBuilderLocal : VersionBuilder {
    private val version: AtomicInteger = AtomicInteger(0)

    override fun buildNextVersion(): Int = version.addAndGet(1)

    override fun getLastVersion(): Int = version.toInt()
}
