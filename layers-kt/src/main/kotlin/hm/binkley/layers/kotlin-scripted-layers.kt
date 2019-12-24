package hm.binkley.layers

import javax.script.ScriptEngine

abstract class KotlinScriptedLayer<
        L : KotlinScriptedLayer<L, LC, LM, LP, LS>,
        LC : KotlinScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : KotlinScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : KotlinScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : KotlinScriptedLayers<L, LC, LM, LP, LS>>(
    slot: Int,
    factory: LS,
    asMutation: (L, MutableValueMap) -> LM
) : XLayer<L, LC, LM, LP, LS>(
    slot,
    factory,
    asMutation
), ScriptedLayer {
    override val included = mutableListOf<String>()

    override fun <R> letEngine(block: (ScriptEngine) -> R): R =
        factory.letEngine(block)

    override fun include(script: String) = included.add(script.clean())
}

abstract class KotlinScriptedLayerCreation<
        L : KotlinScriptedLayer<L, LC, LM, LP, LS>,
        LC : KotlinScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : KotlinScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : KotlinScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : KotlinScriptedLayers<L, LC, LM, LP, LS>>(
    factory: LS,
    asMutation: (L, MutableValueMap) -> LM
) : XLayerCreation<L, LC, LM, LP, LS>(
    factory,
    asMutation
)

abstract class KotlinScriptedLayerMutation<
        L : KotlinScriptedLayer<L, LC, LM, LP, LS>,
        LC : KotlinScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : KotlinScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : KotlinScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : KotlinScriptedLayers<L, LC, LM, LP, LS>>(
    layer: L,
    contents: MutableValueMap
) : XLayerMutation<L, LC, LM, LP, LS>(
    layer,
    contents
), ScriptedLayerMutation {
    override fun execute(script: String): Unit =
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

abstract class KotlinScriptedLayerPersistence<
        L : KotlinScriptedLayer<L, LC, LM, LP, LS>,
        LC : KotlinScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : KotlinScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : KotlinScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : KotlinScriptedLayers<L, LC, LM, LP, LS>>
    : XLayerPersistence<L, LC, LM, LP, LS>()

abstract class KotlinScriptedLayers<
        L : KotlinScriptedLayer<L, LC, LM, LP, LS>,
        LC : KotlinScriptedLayerCreation<L, LC, LM, LP, LS>,
        LM : KotlinScriptedLayerMutation<L, LC, LM, LP, LS>,
        LP : KotlinScriptedLayerPersistence<L, LC, LM, LP, LS>,
        LS : KotlinScriptedLayers<L, LC, LM, LP, LS>>(
    private val scripting: Scripting,
    asCreation: (LS) -> LC,
    asPersistence: (LS) -> LP,
    _layers: MutableList<L>
) : XLayers<L, LC, LM, LP, LS>(
    asCreation,
    asPersistence,
    _layers
), ScriptedLayers {
    override fun <R> letEngine(block: (ScriptEngine) -> R): R =
        scripting.letEngine(block)
}
