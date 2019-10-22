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
        println("commit")
        val layer = Layer()
        layers.add(layer)
        return layer
    }

    private fun applied() = layers.asReversed().flatMap {
        println("applied layer = $it")
        it.asMap().entries
    }.filter {
        println("entry #1 = $it")
        null != it.value.rule
    }.map {
        println("entry #2 = $it")
        val value = it.value.rule!!(it.key, layers)
        println("value = $value")
        SimpleEntry(it.key, value)
    }

    override fun toString() = "${this::class}{layers=$layers}"
}

class Layer(private val contents: MutableMap<String, Value> = mutableMapOf())
    : Map<String, Value> by contents {
    fun update(block: MutableLayer.() -> Unit) = apply {
        val mutable = MutableLayer(contents)
        mutable.block()
    }

    fun asMap(): Map<String, Value> = contents

    override fun toString() = "${this::class}{contents=$contents}"
}

class MutableLayer(private val contents: MutableMap<String, Value>)
    : MutableMap<String, Value> by contents {
    override fun put(key: String, value: Value): Value? {
        println("put $key -> $value")
        return contents.put(key, value)
    }
}

typealias Rule = (key: String, layers: List<Map<String, Value>>) -> Any

open class Value(val rule: Rule?, val context: Any?) {
    override fun toString() =
            "${this::class}{rule=${if (null == rule) null else "<rule>"}, context=$context}"
}

open class ValueValue(context: Any?) : Value(null, context)
open class RuleValue(rule: Rule?) : Value(rule, null)

val YRule: Rule = { key, layers -> 2 }

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
        layers.commit().update {
            getBindings(ENGINE_SCOPE)["layer"] = this@update

            eval("""
                import hm.binkley.layers.*
                
                layer["a"] = RuleValue { key, layers ->
                    2
                }
            """.trimIndent())
        }
        layers.commit().update {
            getBindings(ENGINE_SCOPE)["layer"] = this@update

            eval("""
                import hm.binkley.layers.*

                layer["a"] = ValueValue(3)
            """.trimIndent())
        }
    }

    println(layers)
    println(layers.asMap())
}
