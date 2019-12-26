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
        return InMemoryLayer(slot, factory, asMutation)
    }
}

class InMemoryLayerMutation(layer: InMemoryLayer, contents: MutableValueMap) :
    XLayerMutation<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>(
        layer,
        contents
    )

class InMemoryLayerPersistence :
    XLayerPersistence<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>() {
    override fun load(): List<InMemoryLayer> = listOf()
    override fun commit(layer: InMemoryLayer) = Unit
    override fun rollback(layer: InMemoryLayer) = Unit
}

class InMemoryLayers(
    creation: InMemoryLayerCreation,
    persistence: InMemoryLayerPersistence
) :
    XLayers<InMemoryLayer, InMemoryLayerCreation, InMemoryLayerMutation, InMemoryLayerPersistence, InMemoryLayers>(
        creation,
        persistence
    ) {
    init {
        init()
    }
}
