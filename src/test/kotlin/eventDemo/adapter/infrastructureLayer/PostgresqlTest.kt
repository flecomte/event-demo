package eventDemo.adapter.infrastructureLayer

import eventDemo.testKoinApplicationWithConfig
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import javax.sql.DataSource

class PostgresqlTest :
  FunSpec({
    tags(NamedTag("postgresql"))

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
