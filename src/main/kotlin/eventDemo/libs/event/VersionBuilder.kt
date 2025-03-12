package eventDemo.libs.event

interface VersionBuilder {
    fun buildNextVersion(): Int

    fun getLastVersion(): Int
}
