package dk.rohdef.rfweeks

internal class LazyMap<K, V> private constructor(
    private val actualMap: MutableMap<K, V>,
    private val valueFunction: (K) -> V
) : Map<K, V> by actualMap {
    constructor(valueFunction: (K) -> V) : this(mutableMapOf(), valueFunction)

    override fun get(key: K): V {
        val defaultValue: () -> V = { valueFunction(key) }
        return actualMap.getOrPut(key, defaultValue)
    }
}
