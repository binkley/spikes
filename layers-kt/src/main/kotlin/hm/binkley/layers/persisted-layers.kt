package hm.binkley.layers

interface PersistedForLayers<
        L : XLayer<L, LC, LM, LP, LS>,
        LC : XLayerCreation<L, LC, LM, LP, LS>,
        LM : XLayerMutation<L, LC, LM, LP, LS>,
        LP : PersistedForLayers<L, LC, LM, LP, LS>,
        LS : XLayers<L, LC, LM, LP, LS>> {
    fun load(): List<L>
    fun commit(layer: L)
    fun rollback(layer: L)
}
