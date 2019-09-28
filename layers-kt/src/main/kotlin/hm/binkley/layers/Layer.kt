package hm.binkley.layers

class Layer(private val contents: MutableMap<String, Any?> = mutableMapOf())
    : MutableMap<String, Any?> by contents {
}
