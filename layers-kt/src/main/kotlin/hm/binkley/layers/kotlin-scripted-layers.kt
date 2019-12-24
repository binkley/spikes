package hm.binkley.layers

import javax.script.ScriptEngine

class KotlinScriptedLayer(
    private val factory: ScriptedLayers
) : ScriptedLayer {
    override val included = mutableListOf<String>()

    override fun <R> letEngine(block: (ScriptEngine) -> R): R =
        factory.letEngine(block)

    override fun include(script: String) = included.add(script.clean())
}

class KotlinScriptedLayerMutation<
        L,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : XLayerPersistence<L, LC, LM, LP, LS>,
        LS>(
    private val layer: L
) : ScriptedLayerMutation
        where L : XLayer<L, LC, LM, LP, LS>,
              L : ScriptedLayer,
              LS : XLayers<L, LC, LM, LP, LS>,
              LS : ScriptedLayers {
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

class KotlinScriptedLayers(
    private val scripting: Scripting
) : ScriptedLayers {
    override fun <R> letEngine(block: (ScriptEngine) -> R): R =
        scripting.letEngine(block)
}
