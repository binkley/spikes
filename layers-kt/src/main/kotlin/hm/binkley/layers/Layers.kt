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
    internal fun <T> valueFor(key: String) = layers.asReversed().flatMap {
        it.entries
    }.filter {
        it.key == key
    }.filter {
        null != it.value.rule
    }.first().let {
        (it.value.rule!! as Rule<T>)(RuleContext(key, this))
    }

    @Suppress("UNCHECKED_CAST")
    private fun applied() = layers.asReversed().flatMap {
        it.entries
    }.filter {
        null != it.value.rule
    }.map {
        val key = it.key
        val value = (it.value.rule!! as Rule<Any>)(RuleContext(key, this))
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

class RuleContext<T>(val myKey: String, private val layers: Layers) {
    val myValues: List<T>
        get() = layers.valuesFor(myKey)

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String) = layers.valueFor<T>(key)
}

interface Rule<T> : (RuleContext<T>) -> T {
    override fun toString(): String
}

open class Value(val rule: Rule<*>?, val value: Any?) {
    override fun toString() =
            "${this::class.simpleName}{rule=$rule, value=$value}"
}

open class ValueValue(context: Any?) : Value(null, context)
open class RuleValue<T>(rule: Rule<T>?) : Value(rule, null)

fun <T> rule(name: String, rule: (RuleContext<T>) -> T) =
        RuleValue(object : Rule<T> {
            override fun invoke(context: RuleContext<T>) = rule(context)
            override fun toString() = "<rule: $name>"
        })

fun <T> rule(name: String, default: T, rule: (RuleContext<T>) -> T) =
        Value(object : Rule<T> {
            override fun invoke(context: RuleContext<T>) = rule(context)
            override fun toString() = "<rule: $name>"
        }, default)

fun value(context: Any): Value = ValueValue(context)

fun main() {
    val layers = Layers()
//    layers.commit().edit {
//        this["Q"] = rule("*Anonymous", 0) { context ->
//            if (context["b"]) context.myValues.sum() else -1
//        }
//    }

    val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    with(engine) {
        fun createLayer(script: String) {
            layers.commit().edit {
                eval("""
                    import hm.binkley.layers.*

                    ${script.trimIndent()}
                """.trimIndent(), createBindings().apply {
                    this["layer"] = this@edit
                })
            }
        }

        createLayer("""
                layer["b"] = rule("I am a flag", false) { context ->
                    context.myValues.last()
                }
            """)
        createLayer("""
                layer["b"] = true
            """)
        createLayer("""
                layer["a"] = rule("I am a sum", 0) { context ->
                    if (context["b"]) context.myValues.sum() else -1
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
