package eventDemo.libs.helpers

inline fun <K, V> Map<out K, V>.withReplacedValue(
  toReplace: K,
  transform: (V) -> V,
): Map<K, V> =
  mapValues {
    if (it.key == toReplace) {
      transform(it.value)
    } else {
      it.value
    }
  }

inline fun <V> Set<V>.withReplacedValue(
  toReplace: V,
  transform: (V) -> V,
): Set<V> =
  map {
    if (it == toReplace) {
      transform(it)
    } else {
      it
    }
  }.toSet()
