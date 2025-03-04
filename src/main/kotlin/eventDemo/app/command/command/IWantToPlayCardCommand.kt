package eventDemo.app.command.command

import eventDemo.app.GameState
import eventDemo.app.entity.Card
import eventDemo.app.entity.GameId
import eventDemo.app.entity.Player
import eventDemo.app.event.GameEventStream
import eventDemo.app.event.event.CardIsPlayedEvent
import eventDemo.libs.command.CommandId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A command to perform an action to play a new card
 */
@Serializable
@SerialName("PlayCard")
data class IWantToPlayCardCommand(
    override val payload: Payload,
) : GameCommand {
    override val id: CommandId = CommandId()

    @Serializable
    data class Payload(
        override val gameId: GameId,
        override val player: Player,
        val card: Card,
    ) : GameCommand.Payload

    fun run(
        state: GameState,
        playerNotifier: (String) -> Unit,
        eventStream: GameEventStream,
    ) {
        if (state.canBePlayThisCard()) {
            eventStream.publish(
                CardIsPlayedEvent(
                    payload.gameId,
                    payload.card,
                    payload.player,
                ),
            )
        } else {
            playerNotifier("You cannot play this card")
        }
    }

    fun GameState.canBePlayThisCard(): Boolean {
        if (!isReady) return false
        val cardOnBoard = lastCard?.card ?: return false
        return when (cardOnBoard) {
            is Card.NumericCard -> {
                when (payload.card) {
                    is Card.AllColorCard -> true
                    is Card.NumericCard -> payload.card.number == cardOnBoard.number || payload.card.color == cardOnBoard.color
                    is Card.ColorCard -> payload.card.color == cardOnBoard.color
                }
            }

            is Card.ReverseCard -> {
                when (payload.card) {
                    is Card.ReverseCard -> true
                    is Card.AllColorCard -> true
                    is Card.ColorCard -> payload.card.color == cardOnBoard.color
                }
            }

            is Card.PassCard -> {
                if (payload.player.cardOnBoardIsForYou) {
                    false
                } else {
                    when (payload.card) {
                        is Card.AllColorCard -> true
                        is Card.ColorCard -> payload.card.color == cardOnBoard.color
                    }
                }
            }

            is Card.ChangeColorCard -> {
                when (payload.card) {
                    is Card.AllColorCard -> true
                    is Card.ColorCard -> payload.card.color == lastColor
                }
            }

            is Card.Plus2Card -> {
                if (payload.player.cardOnBoardIsForYou && payload.card is Card.Plus2Card) {
                    true
                } else {
                    when (payload.card) {
                        is Card.AllColorCard -> true
                        is Card.Plus2Card -> true
                        is Card.ColorCard -> payload.card.color == cardOnBoard.color
                    }
                }
            }

            is Card.Plus4Card -> {
                if (payload.player.cardOnBoardIsForYou && payload.card is Card.Plus4Card) {
                    true
                } else {
                    when (payload.card) {
                        is Card.AllColorCard -> true
                        is Card.ColorCard -> payload.card.color == lastColor
                    }
                }
            }
        }
    }
}
