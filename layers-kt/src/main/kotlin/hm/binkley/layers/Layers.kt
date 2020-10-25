package hm.binkley.layers

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
