package dk.rohdef.helperplanning

fun <K, V> MutableMap<K, V>.letValue(key: K, block: (V)->V) {
    this[key] = getValue(key).let(block)
}
