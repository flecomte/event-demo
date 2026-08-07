package eventDemo.testHelpers

import org.koin.core.Koin
import javax.sql.DataSource

fun DataSource.cleanEventSource() {
  this.connection.use {
    it
      .prepareStatement(
        """
        truncate game.game_event_stream;
        truncate auth.user_event_stream;
        truncate auth.user;
        """.trimIndent(),
      ).execute()
  }
}

fun Koin.cleanDataTest() {
  get<DataSource>().cleanEventSource()
}
