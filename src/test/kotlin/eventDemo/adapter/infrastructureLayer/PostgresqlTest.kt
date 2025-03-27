package eventDemo.adapter.infrastructureLayer

import eventDemo.configuration.configure
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.ktor.server.testing.testApplication
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.inject
import javax.sql.DataSource

class PostgresqlTest :
  FunSpec({
    tags(NamedTag("postgresql"))

    test("test connection with postgresql") {
      testApplication {
        application {
          stopKoin()
          configure()

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
    }
  })
