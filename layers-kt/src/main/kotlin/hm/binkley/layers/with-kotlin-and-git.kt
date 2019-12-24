package hm.binkley.layers

typealias WithKotlinAndGitPersistence =
            (WithKotlinAndGitLayers) -> WithKotlinAndGitLayerPersistence

typealias WithKotlinAndGitCreation =
            (WithKotlinAndGitLayers) -> WithKotlinAndGitLayerCreation

typealias WithKotlinAndGitMutation =
            (WithKotlinAndGitLayer, MutableValueMap) ->
        WithKotlinAndGitLayerMutation

class WithKotlinAndGitLayer(
    slot: Int,
    factory: WithKotlinAndGitLayers,
    asMutation: WithKotlinAndGitMutation
) : KotlinScriptedLayer<
        WithKotlinAndGitLayer,
        WithKotlinAndGitLayerCreation,
        WithKotlinAndGitLayerMutation,
        WithKotlinAndGitLayerPersistence,
        WithKotlinAndGitLayers>(
    slot,
    factory,
    asMutation
)

class WithKotlinAndGitLayerCreation(layers: WithKotlinAndGitLayers) :
    KotlinScriptedLayerCreation<
            WithKotlinAndGitLayer,
            WithKotlinAndGitLayerCreation,
            WithKotlinAndGitLayerMutation,
            WithKotlinAndGitLayerPersistence,
            WithKotlinAndGitLayers>(
        layers,
        ::WithKotlinAndGitLayerMutation
    ) {
    override fun new(
        slot: Int
    ): WithKotlinAndGitLayer {
        return WithKotlinAndGitLayer(slot, factory, asMutation)
    }
}

class WithKotlinAndGitLayerMutation(
    layer: WithKotlinAndGitLayer,
    contents: MutableValueMap
) :
    KotlinScriptedLayerMutation<
            WithKotlinAndGitLayer,
            WithKotlinAndGitLayerCreation,
            WithKotlinAndGitLayerMutation,
            WithKotlinAndGitLayerPersistence,
            WithKotlinAndGitLayers>(
        layer,
        contents
    )

class WithKotlinAndGitLayerPersistence :
    KotlinScriptedLayerPersistence<
            WithKotlinAndGitLayer,
            WithKotlinAndGitLayerCreation,
            WithKotlinAndGitLayerMutation,
            WithKotlinAndGitLayerPersistence,
            WithKotlinAndGitLayers>() {
    override fun commit(layer: WithKotlinAndGitLayer) = Unit
    override fun rollback(layer: WithKotlinAndGitLayer) = Unit
}

class WithKotlinAndGitLayers(
    scripting: Scripting,
    asCreation: WithKotlinAndGitCreation,
    asPersistence: WithKotlinAndGitPersistence,
    _layers: MutableList<WithKotlinAndGitLayer>
) :
    KotlinScriptedLayers<
            WithKotlinAndGitLayer,
            WithKotlinAndGitLayerCreation,
            WithKotlinAndGitLayerMutation,
            WithKotlinAndGitLayerPersistence,
            WithKotlinAndGitLayers>(
        scripting,
        asCreation,
        asPersistence,
        _layers
    ) {
    init {
        init()
    }
}
