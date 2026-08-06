package eventDemo.contexts.auth.infrastructure.persistence.projection

import eventDemo.contexts.auth.application.ports.UserProjectionRepository
import eventDemo.contexts.auth.infrastructure.checkPassword
import eventDemo.contexts.auth.infrastructure.hashPassword
import eventDemo.shared.ids.UserId
import javax.sql.DataSource
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

class UserProjectionRepositoryInPostgresql(
  val dataSource: DataSource,
) : UserProjectionRepository {
  override fun getByUsername(username: String): UserProjection? =
    dataSource.connection
      .prepareStatement(
        """
        select id, username 
        from auth."user"
        where id = ?;
        """.trimIndent(),
      ).use {
        it.setObject(1, username)
        it.executeQuery()
      }.use { resultSet ->
        if (resultSet.next()) {
          UserProjection(
            id = UserId(Uuid.parse(resultSet.getString("id"))),
            username = resultSet.getString("username"),
            password = resultSet.getString("password"),
          )
        } else {
          null
        }
      }

  override fun save(user: UserProjection) {
    dataSource.connection.use { connection ->
      connection
        .prepareStatement(
          """
          insert into auth.user (id, username)
          values (?, ?)
          """.trimIndent(),
        ).use {
          it.setObject(1, user.id.id.toJavaUuid())
          it.setString(2, user.username)
          it.executeUpdate()
        }
    }
  }

  override fun getUserIfPasswordIsValid(
    username: String,
    rawPassword: String,
  ): UserProjection? {
    val user = getByUsername(username) ?: return null
    val isValid = checkPassword(rawPassword, hashPassword(user.password))
    if (!isValid) return null
    return user
  }
}
