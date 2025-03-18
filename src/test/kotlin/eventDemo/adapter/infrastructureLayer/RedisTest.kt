package eventDemo.adapter.infrastructureLayer

import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import redis.clients.jedis.JedisPool

class RedisTest :
  FunSpec({
    tags(NamedTag("redis"))

    test("test connection with jedis") {
      JedisPool("redis://localhost:6379").apply {
        resource.set("test", "test")
        resource.get("test") shouldBeEqual "test"
      }
    }
  })
