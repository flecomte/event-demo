package eventDemo.libs.event

interface VersionBuilder {
    fun buildNextVersion(aggregateId: AggregateId): Int

    fun getLastVersion(aggregateId: AggregateId): Int
}
