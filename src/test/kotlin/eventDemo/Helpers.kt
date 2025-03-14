package eventDemo

import eventDemo.app.entity.Card
import eventDemo.app.entity.Deck

fun Deck.allCardCount(): Int =
  stack.size + discard.size + playersHands.values.flatten().size

fun Deck.allCards(): Set<Card> =
  stack + discard + playersHands.values.flatten()

// suspend fun SendChannel<Frame>.send(command: GameCommand) = send(Frame.Text(Json.encodeToString(command)))
