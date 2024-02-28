package eventDemo.app

sealed interface Event<ID : AggregateId> {
    val aggregateId: ID
}

data class PlayCardEvent(
    override val aggregateId: GameId,
    val card: Card,
) : Event<GameId>
