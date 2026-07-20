package eventDemo.domain.event.event

import eventDemo.domain.entity.Deck
import eventDemo.domain.entity.GameId
import eventDemo.domain.entity.Player
import eventDemo.domain.entity.initHands
import eventDemo.configuration.serializer.UUIDSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * This [GameEvent] is sent when all players are ready.
 */
@Serializable
data class GameStartedEvent(
  override val aggregateId: GameId,
  val firstPlayer: Player,
  val deck: Deck,
  override val version: Int,
) : GameEvent {
  @Serializable(with = UUIDSerializer::class)
  override val eventId: UUID = UUID.randomUUID()
  override val createdAt: Instant = Clock.System.now()

  companion object {
    fun new(
      id: GameId,
      players: Set<Player>,
      version: Int,
      shuffleIsDisabled: Boolean = isDisabled,
    ): GameStartedEvent =
      GameStartedEvent(
        aggregateId = id,
        firstPlayer = if (shuffleIsDisabled) players.first() else players.random(),
        deck =
          Deck
            .newWithoutPlayers()
            .let { if (shuffleIsDisabled) it else it.shuffle() }
            .initHands(players)
            .placeFirstCardOnDiscard(),
        version = version,
      )
  }
}

private var isDisabled = false

internal fun disableShuffleDeck() {
  isDisabled = true
}
