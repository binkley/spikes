package x.scratch

import x.scratch.BooleanValue.Companion.lastRule
import x.scratch.IntValue.Companion.sumRule
import x.scratch.Layers.Companion.initialLayer
import x.scratch.StringValue.Companion.lastRule

sealed class Value<T> {
    abstract val value: T
}

interface Rule<T> : (Sequence<T>) -> T

data class BooleanValue(override val value: Boolean) : Value<Boolean>() {
    companion object {
        fun lastRule(initialValue: Boolean) =
            RuleValue(LastRule(), initialValue)
    }
}

data class IntValue(override val value: Int) : Value<Int>() {
    companion object {
        fun sumRule(initialValue: Int) = RuleValue(SumRule(), initialValue)
    }
}

data class StringValue(override val value: String) : Value<String>() {
    companion object {
        fun lastRule(initialValue: String) =
            RuleValue(LastRule(), initialValue)
    }
}

data class RuleValue<T>(val rule: Rule<T>, val initialValue: T) :
    Value<T>(), Rule<T> by rule {
    override val value = initialValue
}

data class Layer(val name: String, val values: Map<String, Value<*>>)

class Layers private constructor(
    initialLayer: Layer
) {
    private val layers: MutableList<Layer> = mutableListOf(initialLayer)

    fun <T> value(key: String): T {
        val allValues = values<T>(key)
        val rule = allValues.filterIsInstance<RuleValue<T>>().last()
        val values = allValues.map { it.value }

        return rule(values)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> values(key: String): Sequence<Value<T>> {
        return layers.map {
            it.values[key]
        }.asSequence().filterNotNull() as Sequence<Value<T>>
    }

    companion object {
        fun initialLayer(vararg rules: Pair<String, RuleValue<*>>) =
            Layers(Layer("initial", mapOf(*rules)))
    }
}

operator fun Layers.get(key: String) = value<Any>(key)

data class LastRule<T>(val name: String = "last") : Rule<T> {
    override fun invoke(values: Sequence<T>) = values.last()
}

data class SumRule(val name: String = "sum") : Rule<Int> {
    override fun invoke(values: Sequence<Int>) =
        values.fold(0) { acc, value ->
            acc + value
        }
}

fun main() {
    val layers = initialLayer(
        "name" to lastRule("The Magnificent Bob"),
        "body" to sumRule(10),
        "mind" to sumRule(10),
        "invisible" to lastRule(false)
    )

    println(layers)
    println(layers["name"])
}
