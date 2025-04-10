package eventDemo.externalServices

import eventDemo.Tag
import eventDemo.testKoinApplicationWithConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import javax.sql.DataSource

class PostgresqlTest :
  FunSpec({
    tags(Tag.Postgresql)

    test("test connection with postgresql") {
      testKoinApplicationWithConfig {
        val datasource by inject<DataSource>()
        datasource.connection.use { connection ->
          connection
            .prepareStatement(
              """
              select 1;
              """.trimIndent(),
            ).execute()
            .let {
              it shouldBeEqual true
            }
        }
      }
    }
  })
