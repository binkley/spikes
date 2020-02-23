package x.scratch.layers

class Layer(
    val name: String,
    val values: MutableMap<String, Value<*>>,
    private val layer: Layers
) {
    override fun toString() = "$name: $values"
}

class Layers private constructor(
    val name: String,
    initialLayerName: String,
    initialLayerValues: MutableMap<String, Value<*>>,
    firstLayerName: String
) {
    private val layers: MutableList<Layer> = mutableListOf(
        Layer(initialLayerName, initialLayerValues, this),
        Layer(firstLayerName, mutableMapOf(), this)
    )

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

    override fun toString(): String {
        val x = StringBuilder(name)
        x.append(':')
        layers.withIndex().reversed().forEach {
            x.append("\n- [${it.index}] ${it.value}")
        }
        return x.toString()
    }

    private fun layer(
        name: String,
        values: MutableMap<String, Value<*>>
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
            Layers(name, "initial", mutableMapOf(*rules), firstLayerName)
    }
}

operator fun Layers.get(key: String) = value<Any>(key)
