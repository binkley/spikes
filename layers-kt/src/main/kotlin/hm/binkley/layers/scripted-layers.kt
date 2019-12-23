package hm.binkley.layers

import javax.script.ScriptEngine

abstract class ScriptedLayer<
        L : ScriptedLayer<L, LC, LM, LP, LS>,
        LC : ScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : ScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : ScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : ScriptedLayers<L, LC, LM, LP, LS>>(
    slot: Int,
    factory: LS,
    asMutation: (L, MutableValueMap) -> LM
) : XLayer<L, LC, LM, LP, LS>(
    slot,
    factory,
    asMutation
) {
    protected val included = mutableListOf<String>()

    internal fun <R> letEngine(block: (ScriptEngine) -> R): R =
        factory.letEngine(block)

    internal fun include(script: String) = included.add(script.clean())
}

abstract class ScriptedLayerCreation<
        L : ScriptedLayer<L, LC, LM, LP, LS>,
        LC : ScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : ScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : ScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : ScriptedLayers<L, LC, LM, LP, LS>>(
    factory: LS,
    asMutation: (L, MutableValueMap) -> LM
) : XLayerCreation<L, LC, LM, LP, LS>(
    factory,
    asMutation
)

abstract class ScriptedLayerMutation<
        L : ScriptedLayer<L, LC, LM, LP, LS>,
        LC : ScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : ScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : ScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : ScriptedLayers<L, LC, LM, LP, LS>>(
    layer: L,
    contents: MutableValueMap
) : XLayerMutation<L, LC, LM, LP, LS>(
    layer,
    contents
) {
    fun execute(script: String): Unit =
        layer.letEngine { engine ->
            engine.eval("""
                    import hm.binkley.layers.*
                    import hm.binkley.layers.rules.*
    
                    $script
                """, engine.createBindings().also {
                it["layer"] = this
            })

            layer.include(script)
        }
}

abstract class ScriptedLayerPersistence<
        L : ScriptedLayer<L, LC, LM, LP, LS>,
        LC : ScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : ScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : ScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : ScriptedLayers<L, LC, LM, LP, LS>>
    : XLayerPersistence<L, LC, LM, LP, LS>() {
    override fun commit(layer: L) = Unit
    override fun rollback(layer: L) = Unit
}

abstract class ScriptedLayers<
        L : ScriptedLayer<L, LC, LM, LP, LS>,
        LC : ScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : ScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : ScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : ScriptedLayers<L, LC, LM, LP, LS>>(
    private val scripting: Scripting,
    asCreation: (LS) -> LC,
    asPersistence: (LS) -> LP,
    _layers: MutableList<L>
) : XLayers<L, LC, LM, LP, LS>(
    asCreation,
    asPersistence,
    _layers
) {
    internal fun <R> letEngine(block: (ScriptEngine) -> R): R =
        scripting.letEngine(block)
}
