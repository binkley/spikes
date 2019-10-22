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

    fun layer(): Layer {
        val layer = Layer()
        layers.add(layer)
        return layer
    }

    private fun applied() = layers.asReversed().asSequence().flatMap {
        it.asMap().entries.asSequence()
    }.filter {
        null != it.value.rule
    }.map {
        SimpleEntry(it.key, it.value.rule!!.compute(it.key, layers))
    }
}

class Layer(
        private val contents: MutableMap<String, Value> = mutableMapOf())
    : Map<String, Value> by contents {
    fun update(block: MutableLayer.() -> Unit) = apply {
        val mutable = MutableLayer()
        mutable.block()
    }

    fun asMap() = contents
}

class MutableLayer(
        private val contents: MutableMap<String, Value> = mutableMapOf())
    : MutableMap<String, Value> by contents {}

interface Rule {
    fun compute(key: String, layers: List<Map<String, Value>>): Any
}

open class Value(val rule: Rule?, val context: Any?)
open class ValueValue(context: Any?) : Value(null, context)
open class RuleValue(rule: Rule?) : Value(rule, null)

fun main() {
    val engine = ScriptEngineManager().getEngineByExtension("kts")!!

    val layers = Layers()
    val layer = layers.layer()

    layer.update {
        with(ScriptEngineManager().getEngineByExtension("kts")!!) {
            this.context = SimpleScriptContext().apply {
                this.setBindings(createBindings().apply {
                    this["layers"] = layers
                    this["layer"] = this@update
                }, ENGINE_SCOPE)
            }

            eval("""
                import hm.binkley.layers.ValueValue

                layer["a"] = ValueValue(3)
            """.trimIndent())
        }
    }

    println(layers.asMap())
}
