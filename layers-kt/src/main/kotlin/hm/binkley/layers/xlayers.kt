package hm.binkley.layers

import lombok.Generated
import java.util.Objects.hash

typealias MutableValueMap = MutableMap<String, Value<*>>

/**
 * To ensure an initial, fresh layer, implementing classes should:
 * ```
 * init {
 *     init()
 * }
 * ```
 *
 * @see init()
 */
abstract class XLayers<
        L : XLayer<L, LC, LM, LP, LS>,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : XLayerPersistence<L, LC, LM, LP, LS>,
        LS : XLayers<L, LC, LM, LP, LS>>(
    private val creation: LC,
    private val persistence: LP
) : LayersForRuleContext {
    private val _layers: MutableList<L> =
        persistence.load().toMutableList()

    /** Please call as part of child class `init` block. */
    protected fun init() {
        // Cannot use `init`: child not yet initialized
        val layer = creation.new(0)
        _layers += layer
    }

    val layers: List<L>
        get() = _layers
    val current: L
        get() = layers[0]

    fun asList(): List<Map<String, Any>> = _layers

    // TODO: Simplify
    fun asMap(): Map<String, Any> =
        _layers.asReversed().flatMap {
            it.entries
        }.filter {
            null != it.value.rule
        }.map {
            val key = it.key
            @Suppress("UNCHECKED_CAST")
            val rule = it.value.rule as Rule<Any>
            val value = rule(RuleContext(key, this))
            key to value
        }.asReversed().toMap().toSortedMap()

    fun commit(): L {
        persistence.commit(current)
        val layer = creation.new(_layers.size)
        _layers += layer
        return current
    }

    fun rollback(): L {
        persistence.rollback(current)
        _layers.removeAt(_layers.lastIndex)
        if (_layers.isEmpty()) init()
        return current
    }

    override fun <T> appliedValueFor(key: String) =
        _layers.asReversed().flatMap {
            it.entries
        }.filter {
            it.key == key
        }.first {
            null != it.value.rule
        }.let {
            @Suppress("UNCHECKED_CAST")
            (it.value.rule!! as Rule<T>)(RuleContext(key, this))
        }

    /** All values for [key] from newest to oldest. */
    @Suppress("UNCHECKED_CAST")
    override fun <T> allValuesFor(key: String) =
        _layers.asReversed().mapNotNull {
            it[key]
        }.mapNotNull {
            it.value
        } as List<T>

    private val self: LS
        @Suppress("UNCHECKED_CAST")
        inline get() = this as LS
}

abstract class XLayer<
        L : XLayer<L, LC, LM, LP, LS>,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : XLayerPersistence<L, LC, LM, LP, LS>,
        LS : XLayers<L, LC, LM, LP, LS>>(
    val slot: Int,
    protected val factory: LS,
    private val asMutation: (L, MutableValueMap) -> LM,
    private val contents: MutableValueMap = sortedMapOf()
) : Diffable,
    Map<String, Value<*>> by contents {
    @Suppress("UNCHECKED_CAST")
    fun edit(block: LM.() -> Unit): L = apply {
        val layer = this as L
        asMutation(layer, contents).block()
    } as L

    override fun toDiff() = contents.entries.joinToString("\n") {
        val (key, value) = it
        "$key: ${value.toDiff()}"
    }

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun equals(other: Any?) = this === other
            || other is XLayer<*, *, *, *, *>
            && slot == other.slot
            && contents == other.contents

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun hashCode() = hash(slot, contents)

    @Generated // Lie to JaCoCo -- why test code for testing?
    override fun toString() =
        "${super.toString()}{slot=$slot, contents=$contents}"
}

abstract class XLayerCreation<
        L : XLayer<L, LC, LM, LP, LS>,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : XLayerPersistence<L, LC, LM, LP, LS>,
        LS : XLayers<L, LC, LM, LP, LS>>(
    protected val factory: LS,
    protected val asMutation: (L, MutableValueMap) -> LM
) {
    abstract fun new(slot: Int): L
}

abstract class XLayerMutation<
        L : XLayer<L, LC, LM, LP, LS>,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : XLayerPersistence<L, LC, LM, LP, LS>,
        LS : XLayers<L, LC, LM, LP, LS>>(
    protected val layer: L,
    protected val contents: MutableValueMap
) : MutableValueMap by contents {
    operator fun <T> set(key: String, value: T) {
        contents[key] = if (value is Value<*>) value else value(value)
    }
}

abstract class XLayerPersistence<
        L : XLayer<L, LC, LM, LP, LS>,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : XLayerPersistence<L, LC, LM, LP, LS>,
        LS : XLayers<L, LC, LM, LP, LS>> {
    abstract fun load(): List<L>
    abstract fun commit(layer: L)
    abstract fun rollback(layer: L)
}
