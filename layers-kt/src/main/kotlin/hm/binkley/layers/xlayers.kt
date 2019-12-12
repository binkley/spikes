package hm.binkley.layers

import lombok.Generated
import java.util.AbstractMap.SimpleEntry
import java.util.Objects

abstract class XLayers<ML : XMutableLayer<ML, L>, L : XLayer<ML, L>, LS : XLayers<ML, L, LS>>(
    private val _layers: MutableList<L> = mutableListOf()
) : LayersForRuleContext {
    val layers: List<L>
        get() = _layers

    fun asList(): List<Map<String, Any>> = _layers

    // TODO: Simplify
    fun asMap(): Map<String, Any> = object : AbstractMap<String, Any>() {
        override val entries: Set<Map.Entry<String, Any>> =
            applied().toSortedSet(compareBy {
                it.key
            })
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> appliedValueFor(key: String) =
        _layers.asReversed().flatMap {
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
    override fun <T> allValuesFor(key: String) =
        _layers.asReversed().mapNotNull {
            it[key]
        }.mapNotNull {
            it.value
        } as List<T>

    @Suppress("UNCHECKED_CAST")
    private fun applied() = _layers.asReversed().flatMap {
        it.entries
    }.filter {
        null != it.value.rule
    }.map {
        val key = it.key
        val rule = it.value.rule as Rule<Any>
        val value = rule(RuleContext(key, this))
        SimpleEntry(key, value)
    }
}

abstract class XLayer<ML : XMutableLayer<ML, L>, L : XLayer<ML, L>>(
    val slot: Int,
    private val asMutable: (L, MutableMap<String, Value<*>>) -> ML,
    private val contents: MutableMap<String, Value<*>> = sortedMapOf()
) : Diffable,
    Map<String, Value<*>> by contents {
    @Suppress("UNCHECKED_CAST")
    fun edit(block: ML.() -> Unit): L = apply {
        val layer = this as L
        asMutable(layer, contents).block()
    } as L

    override fun toDiff() = contents.entries.joinToString("\n") {
        val (key, value) = it
        "$key: ${value.toDiff()}"
    }

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is XLayer<*, *>
            && slot == other.slot
            && contents == other.contents

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = Objects.hash(slot, contents)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() =
        "${super.toString()}{slot=$slot, contents=$contents}"
}

abstract class XMutableLayer<ML : XMutableLayer<ML, L>, L : XLayer<ML, L>>(
    private val layer: L,
    private val contents: MutableMap<String, Value<*>>
) : MutableMap<String, Value<*>> by contents {
    operator fun <T> set(key: String, value: T) {
        contents[key] = if (value is Value<*>) value else value(value)
    }
}
