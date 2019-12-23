package hm.binkley.layers

import javax.script.ScriptEngine

class WithGitLayer(
    slot: Int,
    factory: WithGitLayers,
    asMutation: (WithGitLayer, MutableValueMap) -> WithGitLayerMutation
) : XLayer<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
    slot,
    factory,
    asMutation
) {
    private val included = mutableListOf<String>()

    internal fun <R> letEngine(block: (ScriptEngine) -> R): R =
        factory.letEngine(block)

    internal fun include(script: String) = included.add(script.clean())
}

class WithGitLayerCreation(layers: WithGitLayers) :
    XLayerCreation<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
        layers,
        ::WithGitLayerMutation
    ) {
    override fun new(
        slot: Int
    ): WithGitLayer {
        return WithGitLayer(slot, factory, asMutation)
    }
}

class WithGitLayerMutation(layer: WithGitLayer, contents: MutableValueMap) :
    XLayerMutation<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
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

class WithGitLayerPersistence :
    XLayerPersistence<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>() {
    override fun commit(layer: WithGitLayer) = Unit
    override fun rollback(layer: WithGitLayer) = Unit
}

class WithGitLayers(
    private val scripting: Scripting,
    asCreation: (WithGitLayers) -> WithGitLayerCreation,
    asPersistence: (WithGitLayers) -> WithGitLayerPersistence,
    _layers: MutableList<WithGitLayer>
) :
    XLayers<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
        asCreation,
        asPersistence,
        _layers
    ) {
    init {
        init()
    }

    internal fun <R> letEngine(block: (ScriptEngine) -> R): R =
        scripting.letEngine(block)
}
