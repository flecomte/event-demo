package eventDemo.testHelpers

import eventDemo.contexts.game.domain.game.gameState.GameStarted

fun GameStarted.allCardCount(): Int =
  drawPile.remainingCards + discardPile.size + players.map { it.hand }.sumOf { it.size }
