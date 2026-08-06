package eventDemo

import io.kotest.core.Tag

object Tag {
  object Postgresql : Tag()

  object RabbitMQ : Tag()

  object Concurrence : Tag()
}
