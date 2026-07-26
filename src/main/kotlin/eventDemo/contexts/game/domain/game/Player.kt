package eventDemo.contexts.game.domain.game

import eventDemo.libs.eventSource.AggregateId
import eventDemo.libs.helpers.withReplacedValue
import eventDemo.libs.serializer.UUIDSerializer
import eventDemo.sharedKernel.UserId
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Player(
  val name: String,
  val userId: UserId,
  val hand: PlayerHand = PlayerHand(),
  val id: PlayerId = PlayerId(UUID.randomUUID()),
) {
  @JvmInline
  @Serializable
  value class PlayerId(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
  ) : AggregateId {
    override fun toString(): String =
      id.toString()
  }
}

@Serializable
class PlayerList(
  val players: Set<Player> = emptySet(),
) : Set<Player> by players {
  fun get(id: Player.PlayerId): Player =
    find { it.id == id } ?: error("no player with id $id")

  fun get(id: UserId): Player =
    find { it.userId == id } ?: error("no player with userId $id")

  fun withNewCardOnPlayerHand(
    playerId: Player.PlayerId,
    cards: Set<Card>,
  ): PlayerList =
    players
      .withReplacedValue(get(playerId)) {
        it.copy(
          hand = it.hand.withNewCards(cards),
        )
      }.let { PlayerList(it) }

  fun withDropCardOnPlayerHand(
    playerId: Player.PlayerId,
    card: Card,
  ): PlayerList =
    players
      .withReplacedValue(get(playerId)) {
        it.copy(
          hand = it.hand.withoutTheCards(setOf(card)),
        )
      }.let { PlayerList(it) }

  operator fun plus(player: Player): PlayerList =
    PlayerList(players + player)
}
