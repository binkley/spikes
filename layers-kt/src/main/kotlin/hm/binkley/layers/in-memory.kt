package hm.binkley.layers

class InMemoryLayer(
    slot: Int,
    layers: InMemoryLayers,
    asMutation: (InMemoryLayer, MutableValueMap) -> InMemoryLayerMutation
) :
    XLayer<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>(
        slot,
        layers,
        asMutation
    )

class InMemoryLayerCreation(layers: InMemoryLayers) :
    XLayerCreation<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>(
        layers,
        ::InMemoryLayerMutation
    ) {
    override fun new(
        slot: Int
    ): InMemoryLayer {
        return InMemoryLayer(slot, layers, asMutation)
    }
}

class InMemoryLayerMutation(layer: InMemoryLayer, contents: MutableValueMap) :
    XLayerMutation<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>(
        layer,
        contents
    )

class InMemoryLayerPersistence :
    XLayerPersistence<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>() {
    override fun commit(layer: InMemoryLayer) = Unit
    override fun rollback(layer: InMemoryLayer) = Unit
}

class InMemoryLayers(
    asCreation: (InMemoryLayers) -> InMemoryLayerCreation,
    asPersistence: (InMemoryLayers) -> InMemoryLayerPersistence,
    _layers: MutableList<InMemoryLayer>
) :
    XLayers<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>(
        asCreation,
        asPersistence,
        _layers
    ) {
    init {
        init()
    }
}
