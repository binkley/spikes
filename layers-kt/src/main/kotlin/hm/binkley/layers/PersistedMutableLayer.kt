package hm.binkley.layers

class PersistedMutableLayer(
    override val meta: MutableMap<String, String>,
    private val contents: MutableMap<String, Value<*>>
) : MutableLayer,
    MutableMap<String, Value<*>> by contents {
    @Suppress("UNCHECKED_CAST")
    operator fun <T> set(key: String, value: T) {
        if (value is Value<*>)
            contents[key] = value as Value<T>
        else
            contents[key] = value(value)
    }
}
