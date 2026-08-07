package eventDemo.contexts.game.application.logging

import io.github.oshai.kotlinlogging.withLoggingContext

inline fun <T> withLoggingContext(
  vararg pair: Pair<LoggingContextKeys, *>,
  body: () -> T,
): T =
  withLoggingContext(
    *pair
      .map {
        it.first.name to it.second.toString()
      }.toTypedArray(),
    restorePrevious = true,
    body = body,
  )

// inline fun withLoggingContext(
//  vararg pair: Pair<LoggingContextKeys, *>,
//  body: () -> Unit,
// ) =
//  withLoggingContext(
//    *pair
//      .map {
//        it.first.name to it.second.toString()
//      }.toTypedArray(),
//    restorePrevious = true,
//    body = body,
//  )
