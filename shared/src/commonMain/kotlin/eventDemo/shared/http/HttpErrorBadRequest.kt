package eventDemo.shared.http

import kotlinx.serialization.Serializable

@Serializable
class HttpErrorBadRequest(
  val title: String = "Bad Request",
  val invalidParams: List<InvalidParam> = emptyList(),
) {
  // Hardcoded rather than derived from io.ktor.http.HttpStatusCode.BadRequest, since `shared`
  // (multiplatform, no Ktor dependency) cannot depend on the Ktor server APIs backend uses.
  val statusCode: Int = 400

  @Serializable
  data class InvalidParam(
    val name: String,
    val reason: String,
  )
}
