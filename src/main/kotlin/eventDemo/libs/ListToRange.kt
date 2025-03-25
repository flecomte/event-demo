package eventDemo.libs

fun List<Int>.toRanges(): List<IntRange> =
  fold(listOf()) { acc, i ->
    val last = acc.lastOrNull()
    if (last != null && last.max() + 1 == i) {
      (acc - setOf(last)) + setOf(IntRange(last.min(), i))
    } else {
      acc + setOf(IntRange(i, i))
    }
  }
