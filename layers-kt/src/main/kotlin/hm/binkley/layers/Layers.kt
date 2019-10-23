package hm.binkley.layers

import java.util.AbstractMap.SimpleEntry
import javax.script.ScriptEngineManager
import kotlin.collections.Map.Entry

class Layers(private val layers: MutableList<Layer> = mutableListOf()) {
    fun asMap() = object : AbstractMap<String, Any>() {
        override val entries: Set<Entry<String, Any>>
            get() = applied().toSet()
    }

    fun commit(): Layer {
        val layer = Layer()
        layers.add(layer)
        return layer
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> valuesFor(key: String) = layers.map {
        it[key]
    }.filterNotNull().map {
        it.value
    }.filterNotNull() as List<T>

    @Suppress("UNCHECKED_CAST")
    private fun applied() = layers.asReversed().flatMap {
        it.entries
    }.filter {
        null != it.value.rule
    }.map {
        val key = it.key
        val value = (it.value.rule!! as Rule<Any>)(
                RuleContext(key, valuesFor(key), layers))
        SimpleEntry(key, value)
    }

    override fun toString() = "${this::class.simpleName}$layers"
}

class Layer(private val contents: MutableMap<String, Value> = mutableMapOf())
    : Map<String, Value> by contents {
    fun edit(block: MutableLayer.() -> Unit) = apply {
        val mutable = MutableLayer(contents)
        mutable.block()
    }

    override fun toString() = "${this::class.simpleName}$contents"
}

class MutableLayer(private val contents: MutableMap<String, Value>)
    : MutableMap<String, Value> by contents {
    operator fun set(key: String, value: Any) {
        if (value is Value)
            contents[key] = value
        else
            contents[key] = value(value)
    }
}

data class RuleContext<T>(val key: String, val values: List<T>,
        val layers: List<Map<String, Value>>)

interface Rule<T> : (RuleContext<T>) -> Any {
    override fun toString(): String
}

open class Value(val rule: Rule<*>?, val value: Any?) {
    override fun toString() =
            "${this::class.simpleName}{rule=$rule, value=$value}"
}

open class ValueValue(context: Any?) : Value(null, context)
open class RuleValue<T>(rule: Rule<T>?) : Value(rule, null)

fun <T> rule(name: String, rule: (RuleContext<T>) -> Any): Value =
        RuleValue(object : Rule<T> {
            override fun invoke(context: RuleContext<T>) = rule(context)
            override fun toString() = "<rule: $name>"
        })

fun value(context: Any): Value = ValueValue(context)

fun main() {
    val layers = Layers()

    val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    with(engine) {
        fun createLayer(script: String) {
            layers.commit().edit {
                eval("""
                    import hm.binkley.layers.*

                    ${script.trimIndent()}
                """.trimIndent(), createBindings().apply {
                    this["layers"] = layers
                    this["layer"] = this@edit
                })
            }
        }

        createLayer("""
                layer["a"] = rule<Int>("I am a sum") { context ->
                    context.values.sum()
                }
            """)
        createLayer("""
                layer["a"] = 2
            """)
        createLayer("""
                layer["a"] = 3
            """)
    }

    println(layers)
    println(layers.asMap())
}
