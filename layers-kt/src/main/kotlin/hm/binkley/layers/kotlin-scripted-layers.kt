package hm.binkley.layers

import javax.script.ScriptEngine

class KotlinScriptedForLayer(
    private val factory: ScriptedForLayers
) : ScriptedForLayer {
    override val included = mutableListOf<String>()

    override fun <R> letEngine(block: (ScriptEngine) -> R): R =
        factory.letEngine(block)

    override fun include(script: String) = included.add(script.clean())
}

class KotlinScriptedForLayerMutation<
        L,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : PersistedForLayers<L, LC, LM, LP, LS>,
        LS>(
    private val layer: L
) : ScriptedForLayerMutation
        where L : XLayer<L, LC, LM, LP, LS>,
              L : ScriptedForLayer,
              LS : XLayers<L, LC, LM, LP, LS>,
              LS : ScriptedForLayers {
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

class KotlinScriptedForLayers(
    private val scripting: Scripting
) : ScriptedForLayers {
    override fun <R> letEngine(block: (ScriptEngine) -> R): R =
        scripting.letEngine(block)
}
