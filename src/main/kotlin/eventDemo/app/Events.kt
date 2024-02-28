package eventDemo.app

sealed interface Event<ID : AggregateId> {
    val id: ID
}

data class PlayCardEvent(
    override val id: GameId,
    val card: Card,
) : Event<GameId>
