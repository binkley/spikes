package hm.binkley.layers

/**
 * At creation, a [Layer] begins empty.  Open a _scoped mutation_ with
 * [Layer.edit], and [Layer.commit] when done.  Afterwards, the containing
 * [Layers] will again have a new, scratch layer.
 */
interface Layer
    : Map<String, Value<*>>,
    Diffable {
    val slot: Int
    val script: String
    val enabled: Boolean
    val meta: Map<String, String>

    /**
     * Edits the current layer.
     */
    fun edit(block: MutableLayer.() -> Unit): Layer

    /**
     * Persists the current layer, and returns a fresh, scratch layer.
     */
    fun commit(description: String, notes: String?): Layer
}
