package hm.binkley.layers

import org.junit.jupiter.api.Test

class LayerTest {
    lateinit var layer: Layer

    @Test
    fun setUp() {
        layer = Layer()
    }

    @Test
    fun `should add key-value pair`() {
        layer["foo"] = 3
    }
}
