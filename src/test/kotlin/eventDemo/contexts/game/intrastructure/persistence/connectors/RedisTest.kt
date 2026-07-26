package eventDemo.contexts.game.intrastructure.persistence.connectors

import eventDemo.Tag
import eventDemo.testHelpers.testKoinApplicationWithConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import redis.clients.jedis.UnifiedJedis

class RedisTest :
  FunSpec({
    tags(Tag.Redis)

    test("test connection with jedis") {
      testKoinApplicationWithConfig {
        get<UnifiedJedis>().also {
          it.set("test", "test")
          it.get("test") shouldBeEqual "test"
        }
      }
    }
  })
