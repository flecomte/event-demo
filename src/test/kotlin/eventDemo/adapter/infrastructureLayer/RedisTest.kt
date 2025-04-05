package eventDemo.adapter.infrastructureLayer

import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import redis.clients.jedis.JedisPooled

private val redisUrl = "redis://localhost:6379"

class RedisTest :
  FunSpec({
    tags(NamedTag("redis"))

    xtest("test connection with jedis") {
      JedisPooled(redisUrl).also {
        it.set("test", "test")
        it.get("test") shouldBeEqual "test"
      }
    }
  })
