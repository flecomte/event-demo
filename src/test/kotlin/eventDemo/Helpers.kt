package eventDemo

import eventDemo.business.entity.Card
import eventDemo.business.entity.Deck

fun Deck.allCardCount(): Int =
  stack.size + discard.size + playersHands.values.flatten().size

fun Deck.allCards(): Set<Card> =
  stack + discard + playersHands.values.flatten()
