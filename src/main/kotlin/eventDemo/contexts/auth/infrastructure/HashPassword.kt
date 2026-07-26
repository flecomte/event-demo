package eventDemo.contexts.auth.infrastructure

import com.password4j.Hash
import com.password4j.Password

internal fun hashPassword(password: String): Hash =
  Password.hash(password).addRandomSalt().withArgon2()

internal fun checkPassword(
  password: String,
  hash: Hash,
): Boolean =
  Password.check(password, hash)
