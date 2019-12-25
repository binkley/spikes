package hm.binkley.layers

import org.eclipse.jgit.api.Git

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

/** @todo Call Git */
class WithKotlinAndGitLayerPersistence(
    git: Git
) : XLayerPersistence<
        WithKotlinAndGitLayer,
        WithKotlinAndGitLayerCreation,
        WithKotlinAndGitLayerMutation,
        WithKotlinAndGitLayerPersistence,
        WithKotlinAndGitLayers>() {
    override fun load(): List<WithKotlinAndGitLayer> = listOf()
    override fun commit(layer: WithKotlinAndGitLayer) = Unit
    override fun rollback(layer: WithKotlinAndGitLayer) = Unit
}

class WithKotlinAndGitLayers(
    scripting: Scripting,
    asCreation: WithKotlinAndGitCreation,
    persistence: WithKotlinAndGitLayerPersistence
) : XLayers<
        WithKotlinAndGitLayer,
        WithKotlinAndGitLayerCreation,
        WithKotlinAndGitLayerMutation,
        WithKotlinAndGitLayerPersistence,
        WithKotlinAndGitLayers>(
    asCreation,
    persistence
),
    ScriptedLayers by KotlinScriptedLayers(scripting) {
    init {
        init()
    }
}
