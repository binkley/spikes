package hm.binkley.layers.xx

import java.util.AbstractMap.SimpleEntry

class A(internal val map: MutableMap<String, Value> = mutableMapOf()) :
    Layer<A>,
    Map<String, Any> by map

class AT(private val map: MutableMap<String, Value>) :
    LayerTop<A, AM, AT>,
    Map<String, Any> by map {
    override fun <R> edit(block: AM.() -> R) = AM(map).block()
}

class AM(map: MutableMap<String, Value>) :
    LayerMutation<A, AT, AM>,
    MutableMap<String, Value> by map

class AS(private val layers: MutableList<A> = mutableListOf()) :
    Layers<A, AT, AM, AS> {
    init {
        newLayer()
    }

    override val current: AT
        get() = AT(layers.last().map)

    override fun newLayer(): AT {
        layers.add(newA())
        return current
    }

    override fun asList() = layers

    override fun asMap(): Map<String, Any> {
        return object : AbstractMap<String, Any>() {
            // TODO: Sequences would make more sense here than Lists
            override val entries
                get() = layers.flatMap {
                    it.keys
                }.distinct().map { key ->
                    val rule = layers.mapNotNull {
                        it[key]
                    }.filterIsInstance<Rule<*>>().last()
                    // TODO: Why is Any? inferred, not Any--?
                    val value = rule(key, this@AS)!!
                    SimpleEntry(key, value)
                }.toSet()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> values(key: String) =
        layers.mapNotNull {
            it[key] as Value?
        }.filterIsInstance<Constant<*>>().map {
            it.value as T
        }

    private fun newA() = A()
}

class MostRecentRule<T>(val default: T) : Rule<T> {
    @Suppress("UNCHECKED_CAST")
    override fun invoke(key: String, layers: Layers<*, *, *, *>) =
        layers.values<T>(key).lastOrNull() ?: default
}

class TotalRule : Rule<Int> {
    override fun invoke(key: String, layers: Layers<*, *, *, *>) =
        layers.values<Int>(key).sum()
}

interface Value

interface Constant<T> :
    Value {
    val value: T
}

interface Rule<T> :
    Value {
    operator fun invoke(key: String, layers: Layers<*, *, *, *>): T
}

interface Layer<
        L : Layer<L>> :
    Map<String, Any>

interface LayerTop<
        L : Layer<L>,
        LM : LayerMutation<L, LT, LM>,
        LT : LayerTop<L, LM, LT>> :
    Layer<LT>, // TODO: Be an "L"
    Map<String, Any> {
    fun <R> edit(block: LM.() -> R): R
}

interface LayerMutation<
        L : Layer<L>, // TODO: Only consider LT, not L
        LT : LayerTop<L, LM, LT>,
        LM : LayerMutation<L, LT, LM>> :
    MutableMap<String, Value>

// Too much function collision to be *both* a list and a map
interface Layers<
        L : Layer<L>,
        LT : LayerTop<L, LM, LT>,
        LM : LayerMutation<L, LT, LM>,
        LS : Layers<L, LT, LM, LS>> {
    val current: LT
    fun newLayer(): LT
    fun asList(): List<L>
    fun asMap(): Map<String, Any>
    fun <T> values(key: String): List<T>
}
