package eventDemo.contexts.game.infrastructure.configuration.listener

import eventDemo.contexts.game.application.reaction.ReactionListener
import org.koin.core.Koin

fun Koin.configureReactionListener() {
  get<ReactionListener>()
    .subscribeToBus()
}
