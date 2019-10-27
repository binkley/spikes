package hm.binkley.layers

import java.util.AbstractMap.SimpleEntry
import java.util.Objects
import java.util.TreeMap
import kotlin.collections.Map.Entry

class MutableLayers(private val layers: MutableList<Layer> = mutableListOf())
    : LayersForRuleContext,
        Layers {
    override fun asList(): List<Map<String, Any>> =
            object : AbstractList<Map<String, Any>>() {
                override val size: Int
                    get() = layers.size

                override fun get(index: Int): Map<String, Any> =
                        layers.get(index)
            }

    override fun asMap(): Map<String, Any> =
            object : AbstractMap<String, Any>() {
                override val entries: Set<Entry<String, Any>>
                    get() = applied().toSortedSet(compareBy {
                        it.key
                    })
            }

    fun commit(script: String? = null): Layer {
        val layer = Layer(layers.size, script)
        layers.add(layer)
        return layer
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> valueFor(key: String) = layers.asReversed().flatMap {
        it.entries
    }.filter {
        it.key == key
    }.first {
        null != it.value.rule
    }.let {
        (it.value.rule!! as Rule<T>)(RuleContext(key, this))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> allValuesFor(key: String) = layers.mapNotNull {
        it[key]
    }.mapNotNull {
        it.value
    } as List<T>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MutableLayers

        return layers == other.layers
    }

    override fun hashCode() = Objects.hash(layers)

    override fun toString() = "${this::class.simpleName}$layers"

    @Suppress("UNCHECKED_CAST")
    private fun applied() = layers.asReversed().flatMap {
        it.entries
    }.filter {
        null != it.value.rule
    }.map {
        val key = it.key
        val value =
                (it.value.rule!! as Rule<Any>)(RuleContext(key, this))
        SimpleEntry(key, value)
    }
}

class Layer(val slot: Int,
        val script: String?,
        private val contents: MutableMap<String, Value<*>> = TreeMap())
    : Map<String, Value<*>> by contents {
    fun edit(block: MutableLayer.() -> Unit) = apply {
        val mutable = MutableLayer(contents)
        mutable.block()
    }

    fun forDiff() = contents.entries.map {
        val (key, value) = it
        "$key: ${value.forDiff()}"
    }.joinToString("\n")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Layer

        return slot == other.slot
                && script == other.script
                && contents == other.contents
    }

    override fun hashCode() = Objects.hash(slot, script, contents)

    override fun toString() = "${this::class.simpleName}#$slot:$contents"
}

class MutableLayer(private val contents: MutableMap<String, Value<*>>)
    : MutableMap<String, Value<*>> by contents {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> set(key: String, value: T) {
        if (value is Value<*>)
            contents[key] = value as Value<T>
        else
            contents[key] = value(value)
    }
}

interface LayersForRuleContext {
    fun <T> valueFor(key: String): T

    fun <T> allValuesFor(key: String): List<T>
}

class RuleContext<T>(val myKey: String,
        private val layers: LayersForRuleContext) {
    val myValues: List<T>
        get() = layers.allValuesFor(myKey)

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String) = layers.valueFor<T>(key)
}
