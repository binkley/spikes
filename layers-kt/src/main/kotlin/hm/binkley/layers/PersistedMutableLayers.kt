package hm.binkley.layers

import java.util.AbstractMap.SimpleEntry
import java.util.Objects
import kotlin.collections.Map.Entry

class PersistedMutableLayers(
        private val layers: PersistedLayers,
        private val layerList: MutableList<PersistedLayer> = mutableListOf())
    : MutableLayers,
        LayersForRuleContext {
    override fun asList(): List<Map<String, Any>> = layerList

    override fun asMap(): Map<String, Any> =
            object : AbstractMap<String, Any>() {
                override val entries: Set<Entry<String, Any>>
                    get() = applied().toSortedSet(compareBy {
                        it.key
                    })
            }

    override fun close() = Unit

    fun commit(script: String = ""): Layer {
        val layer = PersistedLayer(layers, layerList.size, script)
        layerList.add(layer)
        return layer
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> appliedValueFor(key: String) = topDownLayers.flatMap {
        it.entries
    }.filter {
        it.key == key
    }.first {
        null != it.value.rule
    }.let {
        (it.value.rule!! as Rule<T>)(RuleContext(key, this))
    }

    /** All values for [key] from newest to oldest. */
    @Suppress("UNCHECKED_CAST")
    override fun <T> allValuesFor(key: String) = topDownLayers.mapNotNull {
        it[key]
    }.mapNotNull {
        it.value
    } as List<T>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistedMutableLayers

        return layerList == other.layerList
    }

    override fun hashCode() = Objects.hash(layerList)

    override fun toString() = "${this::class.simpleName}$layerList"

    @Suppress("UNCHECKED_CAST")
    private fun applied() = topDownLayers.flatMap {
        it.entries
    }.filter {
        null != it.value.rule
    }.map {
        val key = it.key
        val rule = it.value.rule as Rule<Any>
        val value = rule(RuleContext(key, this))
        SimpleEntry(key, value)
    }

    private val topDownLayers: List<PersistedLayer>
        get() = layerList.filter {
            it.enabled
        }.asReversed()
}
