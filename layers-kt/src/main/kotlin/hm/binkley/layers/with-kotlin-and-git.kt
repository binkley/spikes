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
) : XLayer<
        WithKotlinAndGitLayer,
        WithKotlinAndGitLayerCreation,
        WithKotlinAndGitLayerMutation,
        WithKotlinAndGitLayerPersistence,
        WithKotlinAndGitLayers>(
    slot,
    factory,
    asMutation
),
    ScriptedLayer by KotlinScriptedLayer(factory)

class WithKotlinAndGitLayerCreation(
    layers: WithKotlinAndGitLayers
) : XLayerCreation<
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
) : XLayerMutation<
        WithKotlinAndGitLayer,
        WithKotlinAndGitLayerCreation,
        WithKotlinAndGitLayerMutation,
        WithKotlinAndGitLayerPersistence,
        WithKotlinAndGitLayers>(
    layer,
    contents
),
    ScriptedLayerMutation by KotlinScriptedLayerMutation(layer)

class WithKotlinAndGitLayerPersistence :
    XLayerPersistence<
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
) : XLayers<
        WithKotlinAndGitLayer,
        WithKotlinAndGitLayerCreation,
        WithKotlinAndGitLayerMutation,
        WithKotlinAndGitLayerPersistence,
        WithKotlinAndGitLayers>(
    asCreation,
    asPersistence,
    _layers
),
    ScriptedLayers by KotlinScriptedLayers(scripting) {
    init {
        init()
    }
}
