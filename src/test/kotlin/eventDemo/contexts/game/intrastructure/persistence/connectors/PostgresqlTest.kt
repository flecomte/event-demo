package eventDemo.contexts.game.intrastructure.persistence.connectors

import eventDemo.Tag
import eventDemo.testHelpers.testKoinApplicationWithConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import javax.sql.DataSource

class PostgresqlTest :
  FunSpec({
    tags(Tag.Postgresql)

    test("test connection with PostgreSQL") {
      testKoinApplicationWithConfig {
        get<DataSource>().connection.use { connection ->
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
