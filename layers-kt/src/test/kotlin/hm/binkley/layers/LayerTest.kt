package hm.binkley.layers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.MethodOrderer.Alphanumeric
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

@TestMethodOrder(Alphanumeric::class)
internal class LayerTest {
    private val layer = Layer()

    @Test
    fun `should add key-value pair`() {
        layer["foo"] = 3
    }

    @Test
    fun `should have fresh layer`() {
        assertThat(layer).isEmpty()
    }
}
