package hm.binkley.layers

import java.util.AbstractMap.SimpleEntry
import javax.script.ScriptContext.ENGINE_SCOPE
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext
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

    fun valuesFor(key: String) = layers.map {
        it[key]
    }.filterNotNull().map {
        it.value
    }.filterNotNull()

    private fun applied() = layers.asReversed().flatMap {
        it.entries
    }.filter {
        null != it.value.rule
    }.map {
        val key = it.key
        val value = it.value.rule!!(RuleContext(key, valuesFor(key), layers))
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

data class RuleContext(val key: String, val values: List<Any>,
        val layers: List<Map<String, Value>>)

interface Rule : (RuleContext) -> Any {
    override fun toString(): String
}

open class Value(val rule: Rule?, val value: Any?) {
    override fun toString() =
            "${this::class.simpleName}{rule=$rule, value=$value}"
}

open class ValueValue(context: Any?) : Value(null, context)
open class RuleValue(rule: Rule?) : Value(rule, null)

fun rule(name: String, rule: (RuleContext) -> Any): Value =
        RuleValue(object : Rule {
            override fun invoke(context: RuleContext) = rule(context)
            override fun toString() = "<rule: $name>"
        })

fun value(context: Any): Value = ValueValue(context)

fun main() {
    val layers = Layers()

    val engine = ScriptEngineManager().getEngineByExtension("kts")!!.apply {
        context = SimpleScriptContext().apply {
            setBindings(createBindings().apply {
                this["layers"] = layers
            }, ENGINE_SCOPE)
        }
    }

    with(engine) {
        layers.commit().edit {
            getBindings(ENGINE_SCOPE)["layer"] = this@edit

            eval("""
                import hm.binkley.layers.*
                
                layer["a"] = rule("I am a sum") { context ->
                    (context.values as List<Int>).sum()
                }
            """.trimIndent())
        }
        layers.commit().edit {
            getBindings(ENGINE_SCOPE)["layer"] = this@edit

            eval("""
                import hm.binkley.layers.*

                layer["a"] = 2
            """.trimIndent())
        }
        layers.commit().edit {
            getBindings(ENGINE_SCOPE)["layer"] = this@edit

            eval("""
                import hm.binkley.layers.*

                layer["a"] = 3
            """.trimIndent())
        }
    }

    println(layers)
    println(layers.asMap())
}
