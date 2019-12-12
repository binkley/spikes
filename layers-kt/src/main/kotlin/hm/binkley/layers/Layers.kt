package hm.binkley.layers

interface Diffable {
    fun toDiff(): String
}

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

interface MutableLayer : MutableMap<String, Value<*>> {
    val meta: MutableMap<String, String>

    /**
     * TODO: Move this out of mutable, abstract to a higher, "wrapper" over
     * layers
     */
    fun execute(script: String)
}

/*
 * TODO:
 *  Layers - factory, and view, loads on creation(?)
 *  Layer - immutable, has "edit" and "commit"
 *  MutableLayer - mutable, can read from desc/script/notes
 */
/**
 * At creation, a [Layers] begins with a single, scratch layer, ready for
 * editing.
 */
interface Layers : AutoCloseable {
    val layers: List<Layer>

    fun asList(): List<Map<String, Any>>
    fun asMap(): Map<String, Any>

    fun newLayer(description: String, script: String, notes: String?)
            : Layer

    fun newLayer(): Layer
}
