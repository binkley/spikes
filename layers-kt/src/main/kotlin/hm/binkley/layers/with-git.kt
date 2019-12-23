package hm.binkley.layers

class WithGitLayer(
    slot: Int,
    factory: WithGitLayers,
    asMutation: (WithGitLayer, MutableValueMap) -> WithGitLayerMutation
) : ScriptedLayer<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
    slot,
    factory,
    asMutation
)

class WithGitLayerCreation(layers: WithGitLayers) :
    ScriptedLayerCreation<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
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
    ScriptedLayerMutation<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
        layer,
        contents
    )

class WithGitLayerPersistence :
    ScriptedLayerPersistence<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>() {
    override fun commit(layer: WithGitLayer) = Unit
    override fun rollback(layer: WithGitLayer) = Unit
}

class WithGitLayers(
    scripting: Scripting,
    asCreation: (WithGitLayers) -> WithGitLayerCreation,
    asPersistence: (WithGitLayers) -> WithGitLayerPersistence,
    _layers: MutableList<WithGitLayer>
) :
    ScriptedLayers<WithGitLayer, WithGitLayerCreation, WithGitLayerMutation, WithGitLayerPersistence, WithGitLayers>(
        scripting,
        asCreation,
        asPersistence,
        _layers
    ) {
    init {
        init()
    }
}
