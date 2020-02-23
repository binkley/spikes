package x.scratch.layers

class Layer(
    val name: String,
    private val map: MutableMap<String, Value<*>>,
    private val layer: Layers
) : MutableMap<String, Value<*>> by map {
    fun keepAndNext(nextLayerName: String) =
        layer.nextLayer(nextLayerName)

    fun reset(renameLayer: String) =
        layer.reset(renameLayer)

    override fun toString() = "$name: $map"
}

class Layers private constructor(
    val name: String,
    initialLayerValues: MutableMap<String, Value<*>>,
    firstLayerName: String
) {
    private val layers: MutableList<Layer> = mutableListOf()

    val top: Layer
        get() = layers.last()

    init {
        val initialLayer = Layer("initial", initialLayerValues, this)
        layers += initialLayer
        initialLayer.keepAndNext(firstLayerName)
    }

    fun reset(name: String): Layer {
        val newLayer = Layer(name, mutableMapOf(), this)
        layers[layers.size - 1] = newLayer
        return newLayer
    }

    fun <T> value(key: String): T {
        val allValues = values<T>(key)
        val rule = allValues.filterIsInstance<RuleValue<T>>().last()
        val values = allValues.map { it.value }

        return rule(values)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> values(key: String): Sequence<Value<T>> {
        return layers.map {
            it[key]
        }.asSequence().filterNotNull() as Sequence<Value<T>>
    }

    override fun toString(): String {
        val x = StringBuilder(name)
        x.append(':')
        layers.withIndex().reversed().forEach {
            x.append("\n- [${it.index}] ${it.value}")
        }
        return x.toString()
    }

    internal fun nextLayer(
        name: String,
        values: MutableMap<String, Value<*>> = mutableMapOf()
    ): Layer {
        val layer = Layer(name, values, this)
        layers.add(layer)
        return layer
    }

    companion object {
        fun newLayer(
            name: String,
            firstLayerName: String,
            vararg rules: Pair<String, RuleValue<*>>
        ) =
            Layers(name, mutableMapOf(*rules), firstLayerName)
    }
}

operator fun Layers.get(key: String) = value<Any>(key)
