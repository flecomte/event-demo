package eventDemo.shared.ids

import eventDemo.shared.serializers.CommandIdSerializer
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * An ID for the [eventDemo.shared.command.Command]
 */
@JvmInline
@Serializable(with = CommandIdSerializer::class)
value class CommandId(
  private val id: Uuid = Uuid.random(),
) {
  constructor(id: String) : this(Uuid.parse(id))

  override fun toString(): String =
    id.toString()
}
