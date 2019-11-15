package hm.binkley.layers

import java.util.Objects

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
    fun asList(): List<Map<String, Any>>
    fun asMap(): Map<String, Any>

    fun newLayer(description: String, script: String, notes: String?)
            : Layer

    fun refresh()

    fun newLayer(): Layer
}

typealias Rule<T> = (RuleContext<T>) -> T

open class Value<T>(val rule: Rule<T>?, val value: T?) : Diffable {
    // TODO: Print "3 : Int" rather than just "3"
    //  Expression in a class literal has a nullable type 'T', use !! to make
    //  the type non-nullable
    override fun toDiff() = if (null == rule) "$value" else "$this"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Value<*>

        return rule == other.rule
                && value == other.value
    }

    override fun hashCode() =
        Objects.hash(rule, value)

    override fun toString() =
        "${this::class.simpleName}{rule=$rule, value=$value}"
}

open class RuleValue<T>(val name: String, val default: T, rule: Rule<T>) :
    Value<T>(rule, default) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RuleValue<*>

        return name == other.name
                && default == other.default
                && rule == other.rule
    }

    override fun hashCode() =
        Objects.hash(name, default, rule)

    override fun toString() =
        "${this::class.simpleName}<rule: $name[=$default]>"
}

fun <T> value(context: T): Value<T> =
    Value(null, context)

fun <T> rule(name: String, default: T, rule: Rule<T>) =
    RuleValue(name, default, rule)

class RuleContext<T>(
    val myKey: String,
    private val layers: LayersForRuleContext
) {
    val myValues: List<T>
        get() = layers.allValuesFor(myKey)

    operator fun <T> get(key: String) = layers.appliedValueFor<T>(key)
}

interface LayersForRuleContext {
    fun <T> appliedValueFor(key: String): T

    fun <T> allValuesFor(key: String): List<T>
}
