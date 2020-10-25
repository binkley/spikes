package hm.binkley.layers

interface MutableLayer : MutableMap<String, Value<*>> {
    val meta: MutableMap<String, String>

    /**
     * TODO: Move this out of mutable, abstract to a higher, "wrapper" over
     * layers
     */
    fun execute(script: String)
}
